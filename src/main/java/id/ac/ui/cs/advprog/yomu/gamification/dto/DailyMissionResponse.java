package id.ac.ui.cs.advprog.yomu.gamification.dto;

import java.time.LocalDate;

public record DailyMissionResponse(
    String id,
    String name,
    String milestone,
    String missionType,
    Integer targetCount,
    Integer accuracyThreshold,
    Integer requiredCount,
    int rewardScore,
    LocalDate activeFrom,
    LocalDate activeUntil,
    boolean active
) {
}
