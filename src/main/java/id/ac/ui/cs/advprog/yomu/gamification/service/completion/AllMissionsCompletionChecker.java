package id.ac.ui.cs.advprog.yomu.gamification.service.completion;

import java.time.LocalDate;

public interface AllMissionsCompletionChecker {
    boolean areAllActiveDailyMissionsCompleted(String username, LocalDate progressDate);
}
