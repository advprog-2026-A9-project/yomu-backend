package id.ac.ui.cs.advprog.yomu.gamification.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.context.ApplicationEventPublisher;
import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.ProgressUpdateRequest;
import id.ac.ui.cs.advprog.yomu.gamification.event.AllDailyMissionsCompletedEventPublisher;
import id.ac.ui.cs.advprog.yomu.gamification.event.DailyMissionCompletedEvent;
import id.ac.ui.cs.advprog.yomu.gamification.exception.GamificationException;
import id.ac.ui.cs.advprog.yomu.gamification.mapper.GamificationMapper;
import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserDailyMissionProgress;
import id.ac.ui.cs.advprog.yomu.gamification.repository.AchievementRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.DailyMissionRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserAchievementProgressRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserDailyMissionProgressRepository;
import id.ac.ui.cs.advprog.yomu.gamification.strategy.AchievementProgressEvaluator;
import id.ac.ui.cs.advprog.yomu.gamification.validation.GamificationValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ProgressTrackingServiceImpl implements ProgressTrackingService {

    private static final String ACHIEVEMENT_TYPE_READINGS_COMPLETED = "readings_completed";
    private static final String ACHIEVEMENT_TYPE_QUIZZES_PASSED = "quizzes_passed";
    private static final String ACHIEVEMENT_TYPE_ACCURACY_ABOVE = "accuracy_above";
    private static final String ACHIEVEMENT_TYPE_RANKING = "ranking_achieved";
    private static final String MISSION_TYPE_READ_N_ARTICLES = "read_n_articles";

    private final AchievementRepository achievementRepository;
    private final DailyMissionRepository dailyMissionRepository;
    private final UserAchievementProgressRepository userAchievementProgressRepository;
    private final UserDailyMissionProgressRepository userDailyMissionProgressRepository;
    private final GamificationValidator validator;
    private final GamificationMapper mapper;
    private final AllDailyMissionsCompletedEventPublisher allDailyMissionsCompletedEventPublisher;
    private final ApplicationEventPublisher eventPublisher;
    private final List<AchievementProgressEvaluator> achievementEvaluators;

    private boolean evaluateAchievement(UserAchievementProgress progress, Object context) {
        String milestoneType = progress.getAchievement().getMilestoneType();
        for (AchievementProgressEvaluator evaluator : achievementEvaluators) {
            if (evaluator.supports(milestoneType)) {
                return evaluator.evaluate(progress, context);
            }
        }
        return false;
    }

    @Override
    @Transactional
    public AchievementProgressResponse upsertAchievementProgress(ProgressUpdateRequest request) {
        validator.validateMasterId(request.getMasterId());
        validator.validateUsername(request.getUsername());

        String safeMasterId = Objects.requireNonNull(request.getMasterId());
        String safeUsername = Objects.requireNonNull(request.getUsername());

        Achievement achievement = achievementRepository.findById(safeMasterId)
                .orElseThrow(() -> new GamificationException(
                        "Achievement not found",
                        "NOT_FOUND"));

        UserAchievementProgress progress = userAchievementProgressRepository
                .findByUsernameAndAchievement(safeUsername, achievement)
                .orElseGet(() -> {
                    UserAchievementProgress created = new UserAchievementProgress();
                    created.setUsername(safeUsername);
                    created.setAchievement(achievement);
                    return created;
                });

        progress.setProgressValue(request.getProgressValue());

        if (!progress.isUnlocked() && request.getProgressValue() >= achievement.getMilestoneThreshold()) {
            progress.setUnlocked(true);
            progress.setUnlockedAt(LocalDateTime.now());
        }

        UserAchievementProgress saved = userAchievementProgressRepository.save(progress);
        return mapper.toAchievementProgressResponse(saved);
    }

    @Override
    @Transactional
    public DailyMissionProgressResponse upsertDailyMissionProgress(ProgressUpdateRequest request) {
        validator.validateMasterId(request.getMasterId());
        validator.validateUsername(request.getUsername());

        String safeMasterId = Objects.requireNonNull(request.getMasterId());
        String safeUsername = Objects.requireNonNull(request.getUsername());

        DailyMission mission = dailyMissionRepository.findById(safeMasterId)
                .orElseThrow(() -> new GamificationException(
                        "Daily mission not found",
                        "NOT_FOUND"));

        LocalDate today = LocalDate.now();
        boolean completedThisCall = false;

        UserDailyMissionProgress progress = userDailyMissionProgressRepository
                .findByUsernameAndDailyMissionAndProgressDate(safeUsername, mission, today)
                .orElseGet(() -> {
                    UserDailyMissionProgress created = new UserDailyMissionProgress();
                    created.setUsername(safeUsername);
                    created.setDailyMission(mission);
                    created.setProgressDate(today);
                    return created;
                });

        boolean wasCompleted = progress.isCompleted();
        progress.setProgressValue(request.getProgressValue());

        int targetCountVal = mission.getTargetValue();

        if (!progress.isCompleted() && request.getProgressValue() >= targetCountVal) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
            completedThisCall = !wasCompleted;
            if (completedThisCall) {
                eventPublisher.publishEvent(new DailyMissionCompletedEvent(
                        safeUsername, mission.getRewardScore()));
            }
        }

        UserDailyMissionProgress saved = userDailyMissionProgressRepository.save(progress);
        publishAllDailyMissionsCompletedEventIfNeeded(safeUsername, today, completedThisCall);
        return mapper.toDailyMissionProgressResponse(saved);
    }

    @Override
    @Transactional
    public List<AchievementProgressResponse> getAchievementProgressByUsername(String username) {
        validator.validateUsername(username);

        List<Achievement> activeAchievements = achievementRepository.findByActiveTrue();

        return activeAchievements.stream().map(achievement -> {
            UserAchievementProgress progress = userAchievementProgressRepository
                    .findByUsernameAndAchievement(username, achievement)
                    .orElseGet(() -> {
                        UserAchievementProgress empty = new UserAchievementProgress();
                        empty.setUsername(username);
                        empty.setAchievement(achievement);
                        empty.setProgressValue(0);
                        empty.setUnlocked(false);
                        return empty;
                    });
            return mapper.toAchievementProgressResponse(progress);
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyMissionProgressResponse> getTodayDailyMissionProgressByUsername(String username) {
        validator.validateUsername(username);

        LocalDate today = LocalDate.now();
        return userDailyMissionProgressRepository.findByUsernameAndProgressDate(username, today).stream()
                .map(mapper::toDailyMissionProgressResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<DailyMissionProgressResponse> getTodayDailyMissionDashboard(String username) {
        validator.validateUsername(username);
        LocalDate today = LocalDate.now();

        ensureMissionsRotated(today);

        List<DailyMission> activeMissions = dailyMissionRepository
                .findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(today, today);

        return activeMissions.stream().map(mission -> {
            UserDailyMissionProgress progress = userDailyMissionProgressRepository
                    .findByUsernameAndDailyMissionAndProgressDate(username, mission, today)
                    .orElseGet(() -> {
                        UserDailyMissionProgress empty = new UserDailyMissionProgress();
                        empty.setUsername(username);
                        empty.setDailyMission(mission);
                        empty.setProgressDate(today);
                        empty.setProgressValue(0);
                        return empty;
                    });
            return mapper.toDailyMissionProgressResponse(progress);
        }).toList();
    }

    @Override
    @Transactional
    public void handleQuizCompletion(String username, int score) {
        validator.validateUsername(username);
        LocalDate today = LocalDate.now();
        boolean completedThisCall = false;

        // 1. Update Daily Missions
        List<DailyMission> activeMissions = dailyMissionRepository
                .findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(today, today);

        for (DailyMission mission : activeMissions) {
            if (MISSION_TYPE_READ_N_ARTICLES.equals(mission.getMissionType())) {
                continue;
            }
            boolean shouldUpdate = mission.isEligibleForUpdate(score);

            if (shouldUpdate) {
                UserDailyMissionProgress progress = userDailyMissionProgressRepository
                        .findByUsernameAndDailyMissionAndProgressDate(username, mission, today)
                        .orElseGet(() -> {
                            UserDailyMissionProgress created = new UserDailyMissionProgress();
                            created.setUsername(username);
                            created.setDailyMission(mission);
                            created.setProgressDate(today);
                            return created;
                        });

                if (!progress.isCompleted()) {
                    progress.setProgressValue(mission.calculateNewProgressValue(progress.getProgressValue()));
                    int targetCountVal = mission.getTargetValue();

                    if (progress.getProgressValue() >= targetCountVal) {
                        progress.setCompleted(true);
                        progress.setCompletedAt(LocalDateTime.now());
                        completedThisCall = true;
                        eventPublisher.publishEvent(
                                new DailyMissionCompletedEvent(username,
                                        mission.getRewardScore()));
                    }
                    userDailyMissionProgressRepository.save(progress);
                }
            }
        }

        // 2. Update Achievements
        List<Achievement> achievements = achievementRepository.findAll().stream()
                .filter(Achievement::isActive)
                .toList();

        for (Achievement achievement : achievements) {
            UserAchievementProgress progress = userAchievementProgressRepository
                    .findByUsernameAndAchievement(username, achievement)
                    .orElseGet(() -> {
                        UserAchievementProgress created = new UserAchievementProgress();
                        created.setUsername(username);
                        created.setAchievement(achievement);
                        return created;
                    });

            boolean updated = false;
            if (isCountBasedAchievement(achievement.getMilestoneType())) {
                updated = evaluateAchievement(progress, 1);
            } else if (ACHIEVEMENT_TYPE_ACCURACY_ABOVE.equals(achievement.getMilestoneType())) {
                updated = evaluateAchievement(progress, score);
            }

            if (updated) {
                userAchievementProgressRepository.save(progress);
            }
        }

        publishAllDailyMissionsCompletedEventIfNeeded(username, today, completedThisCall);
    }

    @Override
    @Transactional
    public void handleReadingCompletion(String username) {
        validator.validateUsername(username);
        LocalDate today = LocalDate.now();
        boolean completedThisCall = false;

        // 1. Update Daily Missions of type "read_n_articles"
        List<DailyMission> activeMissions = dailyMissionRepository
                .findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(today, today);

        for (DailyMission mission : activeMissions) {
            if (MISSION_TYPE_READ_N_ARTICLES.equals(mission.getMissionType())) {
                UserDailyMissionProgress progress = userDailyMissionProgressRepository
                        .findByUsernameAndDailyMissionAndProgressDate(username, mission, today)
                        .orElseGet(() -> {
                            UserDailyMissionProgress created = new UserDailyMissionProgress();
                            created.setUsername(username);
                            created.setDailyMission(mission);
                            created.setProgressDate(today);
                            return created;
                        });

                if (!progress.isCompleted()) {
                    progress.setProgressValue(mission.calculateNewProgressValue(progress.getProgressValue()));
                    int targetCountVal = mission.getTargetValue();

                    if (progress.getProgressValue() >= targetCountVal) {
                        progress.setCompleted(true);
                        progress.setCompletedAt(LocalDateTime.now());
                        completedThisCall = true;
                        eventPublisher.publishEvent(
                                new DailyMissionCompletedEvent(username,
                                        mission.getRewardScore()));
                    }
                    userDailyMissionProgressRepository.save(progress);
                }
            }
        }

        // 2. Update Achievements of type "readings_completed"
        List<Achievement> achievements = achievementRepository.findAll().stream()
                .filter(Achievement::isActive)
                .filter(a -> ACHIEVEMENT_TYPE_READINGS_COMPLETED.equals(a.getMilestoneType()))
                .toList();

        for (Achievement achievement : achievements) {
            UserAchievementProgress progress = userAchievementProgressRepository
                    .findByUsernameAndAchievement(username, achievement)
                    .orElseGet(() -> {
                        UserAchievementProgress created = new UserAchievementProgress();
                        created.setUsername(username);
                        created.setAchievement(achievement);
                        return created;
                    });

            if (evaluateAchievement(progress, 1)) {
                userAchievementProgressRepository.save(progress);
            }
        }

        publishAllDailyMissionsCompletedEventIfNeeded(username, today, completedThisCall);
    }

    @Override
    @Transactional
    public void handleRankingAchieved(String username, String rankingType, int rank) {
        validator.validateUsername(username);

        List<Achievement> achievements = achievementRepository.findAll().stream()
                .filter(Achievement::isActive)
                .filter(a -> ACHIEVEMENT_TYPE_RANKING.equals(a.getMilestoneType()))
                .toList();

        for (Achievement achievement : achievements) {
            UserAchievementProgress progress = userAchievementProgressRepository
                    .findByUsernameAndAchievement(username, achievement)
                    .orElseGet(() -> {
                        UserAchievementProgress created = new UserAchievementProgress();
                        created.setUsername(username);
                        created.setAchievement(achievement);
                        return created;
                    });

            if (evaluateAchievement(progress, rank)) {
                userAchievementProgressRepository.save(progress);
            }
        }
    }

    private boolean isCountBasedAchievement(String milestoneType) {
        return ACHIEVEMENT_TYPE_QUIZZES_PASSED.equals(milestoneType);
    }

    private void publishAllDailyMissionsCompletedEventIfNeeded(String username, LocalDate progressDate,
            boolean completedThisCall) {
        if (!completedThisCall) {
            return;
        }

        List<DailyMission> activeMissions = dailyMissionRepository
                .findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(progressDate, progressDate);

        if (!activeMissions.isEmpty() && areAllActiveDailyMissionsCompleted(username, progressDate, activeMissions)) {
            allDailyMissionsCompletedEventPublisher.publish(username, progressDate);
        }
    }

    private boolean areAllActiveDailyMissionsCompleted(String username, LocalDate progressDate,
            List<DailyMission> activeMissions) {
        return activeMissions.stream().allMatch(mission -> userDailyMissionProgressRepository
                .findByUsernameAndDailyMissionAndProgressDate(username, mission, progressDate)
                .map(UserDailyMissionProgress::isCompleted)
                .orElse(false));
    }

    private void ensureMissionsRotated(LocalDate today) {
        List<DailyMission> existing = dailyMissionRepository
                .findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(today, today);

        if (!existing.isEmpty()) {
            return;
        }

        List<DailyMission> pool = dailyMissionRepository.findAll().stream()
                .filter(DailyMission::isActive)
                .toList();

        if (pool.isEmpty()) {
            return;
        }

        java.util.List<DailyMission> mutablePool = new java.util.ArrayList<>(pool);
        java.util.Collections.shuffle(mutablePool);
        List<DailyMission> selected = mutablePool.stream().limit(3).toList();

        for (DailyMission mission : selected) {
            mission.setActiveFrom(today);
            mission.setActiveUntil(today);
            dailyMissionRepository.save(mission);
        }
    }

}
