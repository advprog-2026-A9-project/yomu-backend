package id.ac.ui.cs.advprog.yomu.gamification.mapper;

import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionResponse;
import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;
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
        return new AchievementResponse(
            achievement.getId(),
            achievement.getName(),
            achievement.getMilestone(),
            achievement.getMilestoneType(),
            achievement.getMilestoneThreshold(),
            0L,
            achievement.isActive()
        );
    }

    @Override
    public DailyMissionResponse toDailyMissionResponse(DailyMission mission) {
        return new DailyMissionResponse(
            mission.getId(),
            mission.getName(),
            mission.getMilestone(),
            mission.getMissionType(),
            mission.getTargetCount(),
            mission.getRewardDescription(),
            mission.getActiveFrom(),
            mission.getActiveUntil(),
            mission.isActive()
        );
    }

    @Override
    public AchievementProgressResponse toAchievementProgressResponse(UserAchievementProgress progress) {
        return new AchievementProgressResponse(
            progress.getAchievement().getId(),
            progress.getAchievement().getName(),
            progress.getUserId(),
            progress.getProgressValue(),
            progress.isUnlocked()
        );
    }

    @Override
    public DailyMissionProgressResponse toDailyMissionProgressResponse(UserDailyMissionProgress progress) {
        return new DailyMissionProgressResponse(
            progress.getDailyMission().getId(),
            progress.getDailyMission().getName(),
            progress.getUserId(),
            progress.getProgressDate(),
            progress.getProgressValue(),
            progress.isCompleted()
        );
    }
}
