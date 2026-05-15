package id.ac.ui.cs.advprog.yomu.gamification.service;

import java.util.List;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.ProgressUpdateRequest;

public interface ProgressTrackingService {
    AchievementProgressResponse upsertAchievementProgress(ProgressUpdateRequest request);

    DailyMissionProgressResponse upsertDailyMissionProgress(ProgressUpdateRequest request);

    List<AchievementProgressResponse> getAchievementProgressByUserId(String userId);

    List<DailyMissionProgressResponse> getTodayDailyMissionProgressByUserId(String userId);
}
