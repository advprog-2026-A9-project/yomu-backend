package id.ac.ui.cs.advprog.yomu.gamification.event;

import java.util.List;

public record UserShowcaseAchievementChangedEvent(
    String userId,
    List<String> achievementIds
) {}
