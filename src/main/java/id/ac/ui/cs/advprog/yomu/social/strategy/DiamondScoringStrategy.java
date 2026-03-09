package id.ac.ui.cs.advprog.yomu.social.strategy;

import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import org.springframework.stereotype.Component;

/**
 * Diamond tier scoring: Weighted average with special calculations.
 * Uses sophisticated algorithm for top-tier competition.
 */
@Component
public class DiamondScoringStrategy implements ScoringStrategy {

    private static final double WEIGHTED_MULTIPLIER = 1.5;
    private static final double CONSISTENCY_FACTOR = 0.9;

    @Override
    public int calculateScore(Clan clan, int basePoints) {
        // Weighted average: emphasizes consistency
        double weightedScore = basePoints * WEIGHTED_MULTIPLIER * CONSISTENCY_FACTOR;
        return (int) Math.round(weightedScore);
    }

    @Override
    public String getStrategyName() {
        return "Diamond Weighted Average";
    }
}
