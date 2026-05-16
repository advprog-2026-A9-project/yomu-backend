package id.ac.ui.cs.advprog.yomu.gamification.mapper;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionResponse;
import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserDailyMissionProgress;

/**
 * Abstraction for entity-to-DTO mapping
 * Dependency Inversion: high-level modules depend on this interface,
 * not on concrete mapping logic
 */
public interface GamificationMapper {

    /**
     * Map Achievement entity to AchievementResponse DTO
     */
    AchievementResponse toAchievementResponse(Achievement achievement);

    /**
     * Map DailyMission entity to DailyMissionResponse DTO
     */
    DailyMissionResponse toDailyMissionResponse(DailyMission mission);

    /**
     * Map UserAchievementProgress entity to AchievementProgressResponse DTO
     */
    AchievementProgressResponse toAchievementProgressResponse(UserAchievementProgress progress);

    /**
     * Map UserDailyMissionProgress entity to DailyMissionProgressResponse DTO
     */
    DailyMissionProgressResponse toDailyMissionProgressResponse(UserDailyMissionProgress progress);
}
