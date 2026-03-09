package id.ac.ui.cs.advprog.yomu.social.strategy;

import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.social.model.Clan;

/**
 * Bronze tier scoring: Simple sum of all base points.
 * No multipliers or bonuses applied.
 */
@Component
public class BronzeScoringStrategy implements ScoringStrategy {

    @Override
    public int calculateScore(Clan clan, int basePoints) {
        // Simple total: just return base points as-is
        return basePoints;
    }

    @Override
    public String getStrategyName() {
        return "Bronze Simple Sum";
    }
}
