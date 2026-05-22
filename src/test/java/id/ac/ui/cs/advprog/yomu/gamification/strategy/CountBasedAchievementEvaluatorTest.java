package id.ac.ui.cs.advprog.yomu.gamification.strategy;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;

class CountBasedAchievementEvaluatorTest {

    private static final String TYPE_READINGS = "readings_completed";
    private static final String TYPE_QUIZZES = "quizzes_passed";
    private static final String TYPE_OTHER = "ranking_achieved";
    private static final String ACH_NAME = "Book Worm";
    private static final String USERNAME = "user-123";

    // Assertion messages
    private static final String MSG_SUPPORT = "Should support count based types";
    private static final String MSG_NOT_SUPPORT = "Should not support non count based types";
    private static final String MSG_UNLOCKED = "Achievement should be unlocked";
    private static final String MSG_NOT_UNLOCKED = "Achievement should not be unlocked";
    private static final String MSG_PROGRESS_VAL = "Progress value should match";
    private static final String MSG_EVAL_RESULT = "Evaluation result should match";

    private CountBasedAchievementEvaluator evaluator;
    private CountBasedAchievement readingsAchievement;
    private CountBasedAchievement quizzesAchievement;
    private UserAchievementProgress progress;

    @BeforeEach
    void setUp() {
        evaluator = new CountBasedAchievementEvaluator();

        readingsAchievement = new CountBasedAchievement();
        readingsAchievement.setId("ach-read-1");
        readingsAchievement.setName(ACH_NAME);
        readingsAchievement.setMilestoneType(TYPE_READINGS);
        readingsAchievement.setMilestoneThreshold(5);

        quizzesAchievement = new CountBasedAchievement();
        quizzesAchievement.setId("ach-quiz-1");
        quizzesAchievement.setName("Quiz Master");
        quizzesAchievement.setMilestoneType(TYPE_QUIZZES);
        quizzesAchievement.setMilestoneThreshold(3);

        progress = new UserAchievementProgress();
        progress.setUsername(USERNAME);
        progress.setAchievement(readingsAchievement);
        progress.setProgressValue(0);
        progress.setUnlocked(false);
    }

    @Test
    void supports_WhenReadingsCompleted_ShouldReturnTrue() {
        boolean result = evaluator.supports(TYPE_READINGS);
        assertTrue(result, MSG_SUPPORT);
    }

    @Test
    void supports_WhenQuizzesPassed_ShouldReturnTrue() {
        boolean result = evaluator.supports(TYPE_QUIZZES);
        assertTrue(result, MSG_SUPPORT);
    }

    @Test
    void supports_WhenOtherType_ShouldReturnFalse() {
        boolean result = evaluator.supports(TYPE_OTHER);
        assertFalse(result, MSG_NOT_SUPPORT);
    }

    @Test
    void evaluate_WhenAlreadyUnlocked_ShouldReturnFalse() {
        progress.setUnlocked(true);
        boolean result = evaluator.evaluate(progress, new ReadingCompletionContext());
        assertAll("Verify evaluate returns false when already unlocked",
                () -> assertFalse(result, MSG_EVAL_RESULT),
                () -> assertEquals(0, progress.getProgressValue(), MSG_PROGRESS_VAL)
        );
    }

    @Test
    void evaluate_WhenReadingsCompletedAndReadingCompletionContext_ShouldIncrementByOne() {
        ReadingCompletionContext ctx = new ReadingCompletionContext();
        boolean result = evaluator.evaluate(progress, ctx);

        assertAll("Verify progress increments by 1 with ReadingCompletionContext",
                () -> assertTrue(result, MSG_EVAL_RESULT),
                () -> assertEquals(1, progress.getProgressValue(), MSG_PROGRESS_VAL),
                () -> assertFalse(progress.isUnlocked(), MSG_NOT_UNLOCKED)
        );
    }

    @Test
    void evaluate_WhenReadingsCompletedAndIntegerContext_ShouldIncrementByIntegerVal() {
        boolean result = evaluator.evaluate(progress, 3);

        assertAll("Verify progress increments by Integer value",
                () -> assertTrue(result, MSG_EVAL_RESULT),
                () -> assertEquals(3, progress.getProgressValue(), MSG_PROGRESS_VAL),
                () -> assertFalse(progress.isUnlocked(), MSG_NOT_UNLOCKED)
        );
    }

    @Test
    void evaluate_WhenQuizzesPassedAndQuizCompletionContext_ShouldIncrementByOne() {
        progress.setAchievement(quizzesAchievement);
        QuizCompletionContext ctx = new QuizCompletionContext(90);
        boolean result = evaluator.evaluate(progress, ctx);

        assertAll("Verify progress increments by 1 with QuizCompletionContext",
                () -> assertTrue(result, MSG_EVAL_RESULT),
                () -> assertEquals(1, progress.getProgressValue(), MSG_PROGRESS_VAL),
                () -> assertFalse(progress.isUnlocked(), MSG_NOT_UNLOCKED)
        );
    }

    @Test
    void evaluate_WhenQuizzesPassedAndIntegerContext_ShouldIncrementByIntegerVal() {
        progress.setAchievement(quizzesAchievement);
        boolean result = evaluator.evaluate(progress, 2);

        assertAll("Verify progress increments by Integer value for quizzes",
                () -> assertTrue(result, MSG_EVAL_RESULT),
                () -> assertEquals(2, progress.getProgressValue(), MSG_PROGRESS_VAL)
        );
    }

    @Test
    void evaluate_WhenThresholdReached_ShouldUnlock() {
        progress.setProgressValue(4); // threshold is 5

        boolean result = evaluator.evaluate(progress, 1);

        assertAll("Verify achievement unlocks when threshold is met",
                () -> assertTrue(result, MSG_EVAL_RESULT),
                () -> assertEquals(5, progress.getProgressValue(), MSG_PROGRESS_VAL),
                () -> assertTrue(progress.isUnlocked(), MSG_UNLOCKED),
                () -> assertNotNull(progress.getUnlockedAt(), "UnlockedAt timestamp should be populated")
        );
    }

    @Test
    void evaluate_WhenIncrementIsZeroOrNegative_ShouldReturnFalse() {
        boolean result = evaluator.evaluate(progress, 0);
        assertAll("Verify zero increment returns false",
                () -> assertFalse(result, MSG_EVAL_RESULT),
                () -> assertEquals(0, progress.getProgressValue(), MSG_PROGRESS_VAL)
        );
    }

    @Test
    void evaluate_WhenUnknownContextType_ShouldNotIncrement() {
        boolean result = evaluator.evaluate(progress, "unknown-context");
        assertAll("Verify unknown context type returns false",
                () -> assertFalse(result, MSG_EVAL_RESULT),
                () -> assertEquals(0, progress.getProgressValue(), MSG_PROGRESS_VAL)
        );
    }
}
