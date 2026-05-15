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

    private final AchievementRepository achievementRepository;
    private final DailyMissionRepository dailyMissionRepository;
    private final UserAchievementProgressRepository userAchievementProgressRepository;
    private final UserDailyMissionProgressRepository userDailyMissionProgressRepository;
    private final GamificationValidator validator;
    private final GamificationMapper mapper;

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

        UserDailyMissionProgress progress = userDailyMissionProgressRepository
            .findByUserIdAndDailyMissionAndProgressDate(safeUserId, mission, today)
            .orElseGet(() -> {
                UserDailyMissionProgress created = new UserDailyMissionProgress();
                created.setUserId(safeUserId);
                created.setDailyMission(mission);
                created.setProgressDate(today);
                return created;
            });

        progress.setProgressValue(request.getProgressValue());

            if (!progress.isCompleted() && request.getProgressValue() >= mission.getTargetCount()) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
        }

        UserDailyMissionProgress saved = userDailyMissionProgressRepository.save(progress);
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
}

