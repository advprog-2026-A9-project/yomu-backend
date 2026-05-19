package id.ac.ui.cs.advprog.yomu.gamification.dto;

import java.time.LocalDate;

public record DailyMissionProgressResponse(
        String dailyMissionId,
        String dailyMissionName,
        String username,
        LocalDate progressDate,
        int progressValue,
        boolean completed,
        String milestone,
        Integer targetCount,
        Integer accuracyThreshold,
        Integer requiredCount,
        int rewardScore,
        String missionType) {
}
