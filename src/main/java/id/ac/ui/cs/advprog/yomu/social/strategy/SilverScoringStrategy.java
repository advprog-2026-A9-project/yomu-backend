package id.ac.ui.cs.advprog.yomu.social.strategy;

import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.social.model.Clan;

/**
 * Silver tier scoring: Base points with 10% bonus.
 */
@Component
public class SilverScoringStrategy implements ScoringStrategy {

    private static final double BONUS_MULTIPLIER = 1.10;

    @Override
    public int calculateScore(Clan clan, int basePoints) {
        return (int) Math.round(basePoints * BONUS_MULTIPLIER);
    }

    @Override
    public String getStrategyName() {
        return "Silver 10% Bonus";
    }
}
