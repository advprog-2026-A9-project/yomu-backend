package id.ac.ui.cs.advprog.yomu.gamification.strategy;

/**
 * Context record representing a quiz completion event.
 * Allows evaluators to extract parameters like score without service-level branching.
 */
public record QuizCompletionContext(int score) {
}
