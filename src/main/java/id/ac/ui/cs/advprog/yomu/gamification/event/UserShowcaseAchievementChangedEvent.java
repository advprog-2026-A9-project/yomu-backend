package id.ac.ui.cs.advprog.yomu.gamification.event;

import java.util.List;

public record UserShowcaseAchievementChangedEvent(
    String userId,
    List<ShowcaseAchievementInfo> achievements
) {
    public record ShowcaseAchievementInfo(
        String id,
        String name,
        String description,
        String tier,
        String iconColor
    ) {}
}
