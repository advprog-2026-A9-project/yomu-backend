package id.ac.ui.cs.advprog.yomu.social.strategy;

import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import org.springframework.stereotype.Component;

/**
 * Gold tier scoring: Base points with 25% bonus.
 */
@Component
public class GoldScoringStrategy implements ScoringStrategy {

    private static final double BONUS_MULTIPLIER = 1.25;

    @Override
    public int calculateScore(Clan clan, int basePoints) {
        return (int) Math.round(basePoints * BONUS_MULTIPLIER);
    }

    @Override
    public String getStrategyName() {
        return "Gold 25% Bonus";
    }
}
