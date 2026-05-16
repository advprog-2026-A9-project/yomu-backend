package id.ac.ui.cs.advprog.yomu.gamification.service;

import java.util.List;

import id.ac.ui.cs.advprog.yomu.gamification.dto.ShowcaseUpdateRequest;

public interface AchievementShowcaseService {
    List<String> getShowcaseByUserId(String userId);
    void updateShowcase(ShowcaseUpdateRequest request);
}
