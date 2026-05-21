package id.ac.ui.cs.advprog.yomu.gamification.service.achievement;

import java.util.List;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementRequest;
import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementResponse;
import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;

public interface AchievementService {
    AchievementResponse create(AchievementRequest request);

    AchievementResponse update(String achievementId, AchievementRequest request);

    void delete(String achievementId);

    List<AchievementResponse> findAll();

    List<Achievement> getAllAchievements();

    List<Achievement> getAchievementsByIds(List<String> ids);
}
