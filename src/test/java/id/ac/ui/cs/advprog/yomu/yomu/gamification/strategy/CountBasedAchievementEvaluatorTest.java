package id.ac.ui.cs.advprog.yomu.gamification.strategy;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;

class CountBasedAchievementEvaluatorTest {

    private CountBasedAchievementEvaluator evaluator;
    private UserAchievementProgress progress;
    private Achievement achievement;

    @BeforeEach
    void setUp() {
        evaluator = new CountBasedAchievementEvaluator();

        achievement = new Achievement();
        achievement.setMilestoneType("readings_completed");
        achievement.setMilestoneThreshold(5);

        progress = new UserAchievementProgress();
        progress.setAchievement(achievement);
        progress.setProgressValue(0);
        progress.setUnlocked(false);
    }

    @Test
    void testSupports() {
        assertAll("Verify supported milestone types",
                () -> assertTrue(evaluator.supports("readings_completed"), "Should support readings_completed type"),
                () -> assertTrue(evaluator.supports("quizzes_passed"), "Should support quizzes_passed type"),
                () -> assertFalse(evaluator.supports("accuracy_above"), "Should not support accuracy_above type"));
    }

    @Test
    void testEvaluate_AlreadyUnlocked_ShouldReturnFalse() {
        progress.setUnlocked(true);
        assertFalse(evaluator.evaluate(progress, new ReadingCompletionContext()), "Should return false when progress is already unlocked");
    }

    @Test
    void testEvaluate_ReadingsCompleted_WithContext_ShouldIncrementByOne() {
        boolean result = evaluator.evaluate(progress, new ReadingCompletionContext());
        assertAll("Verify increment by context",
                () -> assertTrue(result, "Should return true on successful increment"),
                () -> assertEquals(1, progress.getProgressValue(), "Progress value should be incremented to 1"),
                () -> assertFalse(progress.isUnlocked(), "Should not be unlocked yet"));
    }

    @Test
    void testEvaluate_ReadingsCompleted_WithInteger_ShouldIncrementByValue() {
        boolean result1 = evaluator.evaluate(progress, 3);
        int val1 = progress.getProgressValue();
        boolean unlocked1 = progress.isUnlocked();

        boolean result2 = evaluator.evaluate(progress, 2);
        int val2 = progress.getProgressValue();
        boolean unlocked2 = progress.isUnlocked();
        LocalDateTime unlockedAt = progress.getUnlockedAt();

        assertAll("Verify step-by-step increments",
                () -> assertTrue(result1, "Step 1 evaluation should return true"),
                () -> assertEquals(3, val1, "Step 1 progress value should be 3"),
                () -> assertFalse(unlocked1, "Step 1 should not unlock the achievement"),
                () -> assertTrue(result2, "Step 2 evaluation should return true"),
                () -> assertEquals(5, val2, "Step 2 progress value should be 5"),
                () -> assertTrue(unlocked2, "Step 2 should unlock the achievement"),
                () -> assertNotNull(unlockedAt, "Unlocked timestamp should not be null"));
    }

    @Test
    void testEvaluate_QuizzesPassed_WithContext_ShouldIncrementByOne() {
        achievement.setMilestoneType("quizzes_passed");

        boolean result = evaluator.evaluate(progress, new QuizCompletionContext(80));
        assertAll("Verify quiz completion context progress",
                () -> assertTrue(result, "Should return true on successful increment"),
                () -> assertEquals(1, progress.getProgressValue(), "Progress value should be incremented to 1"));
    }

    @Test
    void testEvaluate_QuizzesPassed_WithInteger_ShouldIncrementByValue() {
        achievement.setMilestoneType("quizzes_passed");

        boolean result = evaluator.evaluate(progress, 4);
        assertAll("Verify quiz completion with integer progress",
                () -> assertTrue(result, "Should return true on successful increment"),
                () -> assertEquals(4, progress.getProgressValue(), "Progress value should be incremented to 4"));
    }

    @Test
    void testEvaluate_IncrementLessOrEqualZero_ShouldReturnFalse() {
        boolean r1 = evaluator.evaluate(progress, 0);
        boolean r2 = evaluator.evaluate(progress, -1);
        boolean r3 = evaluator.evaluate(progress, "wrong_context");

        assertAll("Verify false returns for invalid increments",
                () -> assertFalse(r1, "Should return false for increment of 0"),
                () -> assertFalse(r2, "Should return false for negative increment"),
                () -> assertFalse(r3, "Should return false for unrecognized context type"));
    }
}
