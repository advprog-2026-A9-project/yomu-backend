package id.ac.ui.cs.advprog.yomu.gamification.service;

import java.util.List;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementRequest;
import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementResponse;

public interface AchievementService {
    AchievementResponse create(AchievementRequest request);

    AchievementResponse update(String achievementId, AchievementRequest request);

    void delete(String achievementId);

    List<AchievementResponse> findAll();
}
