package id.ac.ui.cs.advprog.yomu.gamification.dto;

import java.time.LocalDate;

public record DailyMissionProgressResponse(
    String dailyMissionId,
    String dailyMissionName,
    String userId,
    LocalDate progressDate,
    int progressValue,
    boolean completed
) {
}
