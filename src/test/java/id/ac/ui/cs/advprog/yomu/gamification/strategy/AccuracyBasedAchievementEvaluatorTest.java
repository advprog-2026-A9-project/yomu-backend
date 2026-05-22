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

    private static final String MILESTONE_TYPE_ACC = "accuracy_above";
    private static final String MILESTONE_TYPE_OTHER = "count_based";
    private static final String ACH_NAME = "Accuracy Master";
    private static final String USERNAME = "user-123";

    // Assertion messages
    private static final String MSG_SUPPORT = "Should support accuracy_above type";
    private static final String MSG_NOT_SUPPORT = "Should not support other types";
    private static final String MSG_UNLOCKED = "Achievement should be unlocked";
    private static final String MSG_NOT_UNLOCKED = "Achievement should not be unlocked";
    private static final String MSG_PROGRESS_VAL = "Progress value should match";
    private static final String MSG_EVAL_RESULT = "Evaluation result should match";

    private AccuracyBasedAchievementEvaluator evaluator;
    private AccuracyBasedAchievement accuracyAchievement;
    private UserAchievementProgress progress;

    @BeforeEach
    void setUp() {
        evaluator = new AccuracyBasedAchievementEvaluator();

        accuracyAchievement = new AccuracyBasedAchievement();
        accuracyAchievement.setId("ach-acc-1");
        accuracyAchievement.setName(ACH_NAME);
        accuracyAchievement.setMilestoneType(MILESTONE_TYPE_ACC);
        accuracyAchievement.setMilestoneThreshold(3); // Need 3 times
        accuracyAchievement.setAccuracyThreshold(85); // Accuracy >= 85

        progress = new UserAchievementProgress();
        progress.setUsername(USERNAME);
        progress.setAchievement(accuracyAchievement);
        progress.setProgressValue(0);
        progress.setUnlocked(false);
    }

    @Test
    void supports_WhenAccuracyAbove_ShouldReturnTrue() {
        boolean result = evaluator.supports(MILESTONE_TYPE_ACC);
        assertTrue(result, MSG_SUPPORT);
    }

    @Test
    void supports_WhenOtherType_ShouldReturnFalse() {
        boolean result = evaluator.supports(MILESTONE_TYPE_OTHER);
        assertFalse(result, MSG_NOT_SUPPORT);
    }

    @Test
    void evaluate_WhenAlreadyUnlocked_ShouldReturnFalse() {
        progress.setUnlocked(true);
        boolean result = evaluator.evaluate(progress, 90);
        assertAll("Verify evaluate returns false when already unlocked",
                () -> assertFalse(result, MSG_EVAL_RESULT),
                () -> assertEquals(0, progress.getProgressValue(), MSG_PROGRESS_VAL)
        );
    }

    @Test
    void evaluate_WhenQuizCompletionContextAccuracyAboveThreshold_ShouldIncrementAndNotUnlockIfBelowThreshold() {
        QuizCompletionContext ctx = new QuizCompletionContext(90);
        boolean result = evaluator.evaluate(progress, ctx);

        assertAll("Verify progress increment but remains locked",
                () -> assertTrue(result, MSG_EVAL_RESULT),
                () -> assertEquals(1, progress.getProgressValue(), MSG_PROGRESS_VAL),
                () -> assertFalse(progress.isUnlocked(), MSG_NOT_UNLOCKED)
        );
    }

    @Test
    void evaluate_WhenAccuracyIntegerAboveThreshold_ShouldIncrementAndUnlockIfThresholdMet() {
        progress.setProgressValue(2); // 2 of 3 met

        boolean result = evaluator.evaluate(progress, 88); // 88 >= 85

        assertAll("Verify progress increment and unlocks",
                () -> assertTrue(result, MSG_EVAL_RESULT),
                () -> assertEquals(3, progress.getProgressValue(), MSG_PROGRESS_VAL),
                () -> assertTrue(progress.isUnlocked(), MSG_UNLOCKED),
                () -> assertNotNull(progress.getUnlockedAt(), "UnlockedAt timestamp should be populated")
        );
    }

    @Test
    void evaluate_WhenAccuracyBelowThreshold_ShouldNotIncrement() {
        boolean result = evaluator.evaluate(progress, 80); // 80 < 85

        assertAll("Verify no progress change for low accuracy",
                () -> assertFalse(result, MSG_EVAL_RESULT),
                () -> assertEquals(0, progress.getProgressValue(), MSG_PROGRESS_VAL),
                () -> assertFalse(progress.isUnlocked(), MSG_NOT_UNLOCKED)
        );
    }

    @Test
    void evaluate_WhenNotAccuracyBasedAchievementInstance_ShouldUnlockInstantlyOnMeetingThreshold() {
        // Fallback branch: achievement is not AccuracyBasedAchievement, checks threshold on accuracy value directly
        Achievement genericAch = new Achievement();
        genericAch.setId("generic-1");
        genericAch.setMilestoneThreshold(90); // generic checks milestoneThreshold directly
        progress.setAchievement(genericAch);

        boolean result = evaluator.evaluate(progress, 95); // 95 >= 90

        assertAll("Verify generic achievement behavior",
                () -> assertTrue(result, MSG_EVAL_RESULT),
                () -> assertEquals(1, progress.getProgressValue(), MSG_PROGRESS_VAL),
                () -> assertTrue(progress.isUnlocked(), MSG_UNLOCKED)
        );
    }

    @Test
    void evaluate_WhenNotAccuracyBasedAchievementInstanceAndBelowThreshold_ShouldNotUnlock() {
        Achievement genericAch = new Achievement();
        genericAch.setId("generic-1");
        genericAch.setMilestoneThreshold(90);
        progress.setAchievement(genericAch);

        boolean result = evaluator.evaluate(progress, 85); // 85 < 90

        assertAll("Verify generic achievement behavior below threshold",
                () -> assertFalse(result, MSG_EVAL_RESULT),
                () -> assertEquals(0, progress.getProgressValue(), MSG_PROGRESS_VAL),
                () -> assertFalse(progress.isUnlocked(), MSG_NOT_UNLOCKED)
        );
    }
}
