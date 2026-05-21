package id.ac.ui.cs.advprog.yomu.gamification.strategy;

/**
 * Context record for ranking evaluation.
 * Decoupled from concrete evaluator class.
 */
public record RankingContext(int rank, String tier) {
}
