package id.ac.ui.cs.advprog.yomu.gamification.dto;

public record AchievementResponse(
    String id,
    String name,
    String milestone,
    String milestoneType,
    int milestoneThreshold,
    long earnedCount,
    boolean active
) {
}
