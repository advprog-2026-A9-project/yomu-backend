package id.ac.ui.cs.advprog.yomu.gamification.event;

import java.time.LocalDate;

public record AllDailyMissionsCompletedEvent(
    String userId,
    LocalDate progressDate
) {}