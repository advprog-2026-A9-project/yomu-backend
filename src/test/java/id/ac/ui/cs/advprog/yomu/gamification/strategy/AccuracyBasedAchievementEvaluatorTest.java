package id.ac.ui.cs.advprog.yomu.gamification.strategy;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import id.ac.ui.cs.advprog.yomu.gamification.model.AccuracyBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;

class AccuracyBasedAchievementEvaluatorTest {

    private AccuracyBasedAchievementEvaluator evaluator;
    private UserAchievementProgress progress;
    private AccuracyBasedAchievement accuracyAchievement;

    @BeforeEach
    void setUp() {
        evaluator = new AccuracyBasedAchievementEvaluator();

        accuracyAchievement = new AccuracyBasedAchievement();
        accuracyAchievement.setMilestoneType("accuracy_above");
        accuracyAchievement.setAccuracyThreshold(90);
        accuracyAchievement.setMilestoneThreshold(3);

        progress = new UserAchievementProgress();
        progress.setAchievement(accuracyAchievement);
        progress.setProgressValue(0);
        progress.setUnlocked(false);
    }

    @Test
    void testSupports() {
        assertAll("Verify supported types",
                () -> assertTrue(evaluator.supports("accuracy_above"), "Should support accuracy_above type"),
                () -> assertFalse(evaluator.supports("readings_completed"), "Should not support readings_completed type"));
    }

    @Test
    void testEvaluate_AlreadyUnlocked_ShouldReturnFalse() {
        progress.setUnlocked(true);
        assertFalse(evaluator.evaluate(progress, 95), "Should return false when progress is already unlocked");
    }

    @Test
    void testEvaluate_WhenAccuracyBelowThreshold_ShouldNotIncrementOrUnlock() {
        boolean result = evaluator.evaluate(progress, 89);
        assertAll("Verify no progress or unlock on low accuracy",
                () -> assertFalse(result, "Should return false when accuracy is below threshold"),
                () -> assertEquals(0, progress.getProgressValue(), "Progress value should remain 0"),
                () -> assertFalse(progress.isUnlocked(), "Should not unlock progress"));
    }

    @Test
    void testEvaluate_WhenAccuracyAtOrAboveThreshold_ShouldIncrementAndUnlockIfThresholdReached() {
        boolean r1 = evaluator.evaluate(progress, 90);
        int val1 = progress.getProgressValue();
        boolean unlocked1 = progress.isUnlocked();

        boolean r2 = evaluator.evaluate(progress, new QuizCompletionContext(95));
        int val2 = progress.getProgressValue();
        boolean unlocked2 = progress.isUnlocked();

        boolean r3 = evaluator.evaluate(progress, 98);
        int val3 = progress.getProgressValue();
        boolean unlocked3 = progress.isUnlocked();
        LocalDateTime unlockedAt = progress.getUnlockedAt();

        assertAll("Verify step-by-step evaluations and status changes",
                () -> assertTrue(r1, "Step 1 should return true"),
                () -> assertEquals(1, val1, "Step 1 progress value should be 1"),
                () -> assertFalse(unlocked1, "Step 1 should not unlock the achievement"),
                () -> assertTrue(r2, "Step 2 should return true"),
                () -> assertEquals(2, val2, "Step 2 progress value should be 2"),
                () -> assertFalse(unlocked2, "Step 2 should not unlock the achievement"),
                () -> assertTrue(r3, "Step 3 should return true"),
                () -> assertEquals(3, val3, "Step 3 progress value should be 3"),
                () -> assertTrue(unlocked3, "Step 3 should unlock the achievement"),
                () -> assertNotNull(unlockedAt, "Unlocked timestamp should not be null"));
    }

    @Test
    void testEvaluate_WithStandardNonAccuracyAchievementInProgress() {
        Achievement standardAchievement = new Achievement();
        standardAchievement.setMilestoneThreshold(100);
        progress.setAchievement(standardAchievement);

        boolean r1 = evaluator.evaluate(progress, 99);
        int val1 = progress.getProgressValue();
        boolean unlocked1 = progress.isUnlocked();

        boolean r2 = evaluator.evaluate(progress, 100);
        int val2 = progress.getProgressValue();
        boolean unlocked2 = progress.isUnlocked();

        assertAll("Verify standard achievement fallback evaluation",
                () -> assertFalse(r1, "Should return false if accuracy is below threshold"),
                () -> assertEquals(0, val1, "Progress value should remain 0 on check 1"),
                () -> assertFalse(unlocked1, "Should not be unlocked on check 1"),
                () -> assertTrue(r2, "Should return true if accuracy meets threshold"),
                () -> assertEquals(1, val2, "Progress value should be incremented on check 2"),
                () -> assertTrue(unlocked2, "Should be unlocked on check 2"));
    }
}
