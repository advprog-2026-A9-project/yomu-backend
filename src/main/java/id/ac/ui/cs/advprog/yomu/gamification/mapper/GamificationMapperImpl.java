package id.ac.ui.cs.advprog.yomu.gamification.mapper;

import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionResponse;
import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.AccuracyBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.AccuracyDailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedDailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserDailyMissionProgress;

/**
 * Implementation of gamification mapper
 * Dependency Inversion: concrete mapping logic is decoupled from services
 */
@Component
public class GamificationMapperImpl implements GamificationMapper {

    @Override
    public AchievementResponse toAchievementResponse(Achievement achievement) {
        Integer accuracyThreshold = null;
        if (achievement instanceof AccuracyBasedAchievement accuracyAchievement) {
            accuracyThreshold = accuracyAchievement.getAccuracyThreshold();
        }
        return new AchievementResponse(
            achievement.getId(),
            achievement.getName(),
            achievement.getMilestone(),
            achievement.getMilestoneType(),
            achievement.getMilestoneThreshold(),
            accuracyThreshold,
            achievement.getTier(),
            0L,
            achievement.isActive()
        );
    }

    @Override
    public DailyMissionResponse toDailyMissionResponse(DailyMission mission) {
        Integer targetCount = null;
        Integer accuracyThreshold = null;
        Integer requiredCount = null;

        if (mission instanceof CountBasedDailyMission countMission) {
            targetCount = countMission.getTargetCount();
        } else if (mission instanceof AccuracyDailyMission accuracyMission) {
            accuracyThreshold = accuracyMission.getAccuracyThreshold();
            requiredCount = accuracyMission.getRequiredCount();
        }

        return new DailyMissionResponse(
            mission.getId(),
            mission.getName(),
            mission.getMilestone(),
            mission.getMissionType(),
            targetCount,
            accuracyThreshold,
            requiredCount,
            mission.getRewardScore(),
            mission.getActiveFrom(),
            mission.getActiveUntil(),
            mission.isActive()
        );
    }

    @Override
    public AchievementProgressResponse toAchievementProgressResponse(UserAchievementProgress progress) {
        Integer accuracyThreshold = null;
        if (progress.getAchievement() instanceof AccuracyBasedAchievement accuracyAchievement) {
            accuracyThreshold = accuracyAchievement.getAccuracyThreshold();
        }
        return new AchievementProgressResponse(
            progress.getAchievement().getId(),
            progress.getAchievement().getName(),
            progress.getUsername(),
            progress.getProgressValue(),
            progress.isUnlocked(),
            progress.getAchievement().getMilestone(),
            progress.getAchievement().getMilestoneType(),
            progress.getAchievement().getMilestoneThreshold(),
            accuracyThreshold,
            progress.getAchievement().getTier()
        );
    }

    @Override
    public DailyMissionProgressResponse toDailyMissionProgressResponse(UserDailyMissionProgress progress) {
        DailyMission mission = progress.getDailyMission();
        Integer targetCount = null;
        Integer accuracyThreshold = null;
        Integer requiredCount = null;

        if (mission instanceof CountBasedDailyMission countMission) {
            targetCount = countMission.getTargetCount();
        } else if (mission instanceof AccuracyDailyMission accuracyMission) {
            accuracyThreshold = accuracyMission.getAccuracyThreshold();
            requiredCount = accuracyMission.getRequiredCount();
        }

        return new DailyMissionProgressResponse(
            mission.getId(),
            mission.getName(),
            progress.getUsername(),
            progress.getProgressDate(),
            progress.getProgressValue(),
            progress.isCompleted(),
            mission.getMilestone(),
            targetCount,
            accuracyThreshold,
            requiredCount,
            mission.getRewardScore(),
            mission.getMissionType()
        );
    }
}
