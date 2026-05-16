package id.ac.ui.cs.advprog.yomu.gamification.dto;

public record DailyMissionResponse(
    String id,
    String name,
    String milestone,
    String missionType,
    int targetCount,
    String rewardDescription,
    java.time.LocalDate activeFrom,
    java.time.LocalDate activeUntil,
    boolean active
) {
}
