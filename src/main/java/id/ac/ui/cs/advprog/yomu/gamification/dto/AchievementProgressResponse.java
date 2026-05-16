package id.ac.ui.cs.advprog.yomu.gamification.dto;

public record AchievementProgressResponse(
    String achievementId,
    String achievementName,
    String userId,
    int progressValue,
    boolean unlocked,
    String milestone,
    String milestoneType,
    int milestoneThreshold
) {
}
