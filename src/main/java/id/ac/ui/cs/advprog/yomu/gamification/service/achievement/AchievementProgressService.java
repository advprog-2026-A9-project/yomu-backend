package id.ac.ui.cs.advprog.yomu.gamification.service.achievement;

import java.util.List;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.ProgressUpdateRequest;
import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;

public interface AchievementProgressService {
    AchievementProgressResponse upsertAchievementProgress(ProgressUpdateRequest request);

    List<AchievementProgressResponse> getAchievementProgressByUsername(String username);

    UserAchievementProgress getOrCreateAchievementProgress(String username, Achievement achievement);

    void saveProgress(UserAchievementProgress progress);
}
