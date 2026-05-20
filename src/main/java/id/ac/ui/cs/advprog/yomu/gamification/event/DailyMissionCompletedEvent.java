package id.ac.ui.cs.advprog.yomu.gamification.event;

public record DailyMissionCompletedEvent(
    String username,
    int score
) {}
