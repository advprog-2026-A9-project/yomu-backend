package id.ac.ui.cs.advprog.yomu.gamification.service;

import java.util.List;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.ProgressUpdateRequest;

public interface ProgressTrackingService {
    AchievementProgressResponse upsertAchievementProgress(ProgressUpdateRequest request);

    DailyMissionProgressResponse upsertDailyMissionProgress(ProgressUpdateRequest request);

    List<AchievementProgressResponse> getAchievementProgressByUsername(String username);

    List<DailyMissionProgressResponse> getTodayDailyMissionProgressByUsername(String username);
    List<DailyMissionProgressResponse> getTodayDailyMissionDashboard(String username);
    void handleQuizCompletion(String username, int score);
    void handleReadingCompletion(String username);
    void handleRankingAchieved(String username, String rankingType, int rank);
}
