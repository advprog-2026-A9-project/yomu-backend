package id.ac.ui.cs.advprog.yomu.gamification.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.ProgressUpdateRequest;
import id.ac.ui.cs.advprog.yomu.gamification.event.AllDailyMissionsCompletedEventPublisher;
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
import id.ac.ui.cs.advprog.yomu.gamification.validation.GamificationValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProgressTrackingServiceImpl implements ProgressTrackingService {

    private static final String MISSION_TYPE_READ_ARTICLES = "read_n_articles";
    private static final String MISSION_TYPE_COMPLETE_QUIZZES = "complete_n_quizzes";
    private static final String MISSION_TYPE_ACCURACY = "achieve_accuracy";
    private static final String ACHIEVEMENT_TYPE_READINGS_COMPLETED = "readings_completed";
    private static final String ACHIEVEMENT_TYPE_QUIZZES_PASSED = "quizzes_passed";
    private static final String ACHIEVEMENT_TYPE_ACCURACY_ABOVE = "accuracy_above";

    private final AchievementRepository achievementRepository;
    private final DailyMissionRepository dailyMissionRepository;
    private final UserAchievementProgressRepository userAchievementProgressRepository;
    private final UserDailyMissionProgressRepository userDailyMissionProgressRepository;
    private final GamificationValidator validator;
    private final GamificationMapper mapper;
    private final AllDailyMissionsCompletedEventPublisher allDailyMissionsCompletedEventPublisher;

    @Override
    @Transactional
    public AchievementProgressResponse upsertAchievementProgress(ProgressUpdateRequest request) {
        validator.validateMasterId(request.getMasterId());
        validator.validateUserId(request.getUserId());

        String safeMasterId = Objects.requireNonNull(request.getMasterId());
        String safeUserId = Objects.requireNonNull(request.getUserId());

        Achievement achievement = achievementRepository.findById(safeMasterId)
            .orElseThrow(() -> new GamificationException(
                "Achievement not found",
                "NOT_FOUND"
            ));

        UserAchievementProgress progress = userAchievementProgressRepository
            .findByUserIdAndAchievement(safeUserId, achievement)
            .orElseGet(() -> {
                UserAchievementProgress created = new UserAchievementProgress();
                created.setUserId(safeUserId);
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
        validator.validateUserId(request.getUserId());

        String safeMasterId = Objects.requireNonNull(request.getMasterId());
        String safeUserId = Objects.requireNonNull(request.getUserId());

        DailyMission mission = dailyMissionRepository.findById(safeMasterId)
            .orElseThrow(() -> new GamificationException(
                "Daily mission not found",
                "NOT_FOUND"
            ));

        LocalDate today = LocalDate.now();
        boolean completedThisCall = false;

        UserDailyMissionProgress progress = userDailyMissionProgressRepository
            .findByUserIdAndDailyMissionAndProgressDate(safeUserId, mission, today)
            .orElseGet(() -> {
                UserDailyMissionProgress created = new UserDailyMissionProgress();
                created.setUserId(safeUserId);
                created.setDailyMission(mission);
                created.setProgressDate(today);
                return created;
            });

        boolean wasCompleted = progress.isCompleted();
        progress.setProgressValue(request.getProgressValue());

        if (!progress.isCompleted() && request.getProgressValue() >= mission.getTargetCount()) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
            completedThisCall = !wasCompleted;
        }

        UserDailyMissionProgress saved = userDailyMissionProgressRepository.save(progress);
        publishAllDailyMissionsCompletedEventIfNeeded(safeUserId, today, completedThisCall);
        return mapper.toDailyMissionProgressResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementProgressResponse> getAchievementProgressByUserId(String userId) {
        validator.validateUserId(userId);

        return userAchievementProgressRepository.findByUserId(userId).stream()
            .map(mapper::toAchievementProgressResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyMissionProgressResponse> getTodayDailyMissionProgressByUserId(String userId) {
        validator.validateUserId(userId);

        LocalDate today = LocalDate.now();
        return userDailyMissionProgressRepository.findByUserIdAndProgressDate(userId, today).stream()
            .map(mapper::toDailyMissionProgressResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyMissionProgressResponse> getTodayDailyMissionDashboard(String userId) {
        validator.validateUserId(userId);
        LocalDate today = LocalDate.now();

        List<DailyMission> activeMissions = dailyMissionRepository
            .findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(today, today);

        return activeMissions.stream().map(mission -> {
            UserDailyMissionProgress progress = userDailyMissionProgressRepository
                .findByUserIdAndDailyMissionAndProgressDate(userId, mission, today)
                .orElseGet(() -> {
                    UserDailyMissionProgress empty = new UserDailyMissionProgress();
                    empty.setUserId(userId);
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
    public void handleQuizCompletion(String userId, int score) {
        validator.validateUserId(userId);
        LocalDate today = LocalDate.now();
        boolean completedThisCall = false;

        // 1. Update Daily Missions
        List<DailyMission> activeMissions = dailyMissionRepository
            .findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(today, today);

        for (DailyMission mission : activeMissions) {
            boolean shouldUpdate = false;
            int increment = 0;

            if (isCountBasedMission(mission.getMissionType())) {
                shouldUpdate = true;
                increment = 1;
            } else if (MISSION_TYPE_ACCURACY.equals(mission.getMissionType())) {
                if (score >= mission.getTargetCount()) { // Using targetCount as threshold for accuracy
                    shouldUpdate = true;
                    increment = 1;
                }
            }

            if (shouldUpdate) {
                UserDailyMissionProgress progress = userDailyMissionProgressRepository
                    .findByUserIdAndDailyMissionAndProgressDate(userId, mission, today)
                    .orElseGet(() -> {
                        UserDailyMissionProgress created = new UserDailyMissionProgress();
                        created.setUserId(userId);
                        created.setDailyMission(mission);
                        created.setProgressDate(today);
                        return created;
                    });

                if (!progress.isCompleted()) {
                    progress.setProgressValue(progress.getProgressValue() + increment);
                    if (progress.getProgressValue() >= mission.getTargetCount()) {
                        progress.setCompleted(true);
                        progress.setCompletedAt(LocalDateTime.now());
                        completedThisCall = true;
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
            boolean shouldUpdate = false;
            int increment = 0;

            if (isCountBasedAchievement(achievement.getMilestoneType())) {
                shouldUpdate = true;
                increment = 1;
            } else if (ACHIEVEMENT_TYPE_ACCURACY_ABOVE.equals(achievement.getMilestoneType())) {
                if (score >= achievement.getMilestoneThreshold()) {
                    shouldUpdate = true;
                    increment = 1;
                }
            }

            if (shouldUpdate) {
                UserAchievementProgress progress = userAchievementProgressRepository
                    .findByUserIdAndAchievement(userId, achievement)
                    .orElseGet(() -> {
                        UserAchievementProgress created = new UserAchievementProgress();
                        created.setUserId(userId);
                        created.setAchievement(achievement);
                        return created;
                    });

                if (!progress.isUnlocked()) {
                    progress.setProgressValue(progress.getProgressValue() + increment);
                    if (progress.getProgressValue() >= achievement.getMilestoneThreshold()) {
                        progress.setUnlocked(true);
                        progress.setUnlockedAt(LocalDateTime.now());
                    }
                    userAchievementProgressRepository.save(progress);
                }
            }
        }

        publishAllDailyMissionsCompletedEventIfNeeded(userId, today, completedThisCall);
    }

    private boolean isCountBasedMission(String missionType) {
        return MISSION_TYPE_READ_ARTICLES.equals(missionType) || MISSION_TYPE_COMPLETE_QUIZZES.equals(missionType);
    }

    private boolean isCountBasedAchievement(String milestoneType) {
        return ACHIEVEMENT_TYPE_READINGS_COMPLETED.equals(milestoneType)
            || ACHIEVEMENT_TYPE_QUIZZES_PASSED.equals(milestoneType);
    }

    private void publishAllDailyMissionsCompletedEventIfNeeded(String userId, LocalDate progressDate, boolean completedThisCall) {
        if (!completedThisCall) {
            return;
        }

        List<DailyMission> activeMissions = dailyMissionRepository
            .findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(progressDate, progressDate);

        if (!activeMissions.isEmpty() && areAllActiveDailyMissionsCompleted(userId, progressDate, activeMissions)) {
            allDailyMissionsCompletedEventPublisher.publish(userId, progressDate);
        }
    }

    private boolean areAllActiveDailyMissionsCompleted(String userId, LocalDate progressDate, List<DailyMission> activeMissions) {
        return activeMissions.stream().allMatch(mission -> userDailyMissionProgressRepository
            .findByUserIdAndDailyMissionAndProgressDate(userId, mission, progressDate)
            .map(UserDailyMissionProgress::isCompleted)
            .orElse(false));
    }
}

