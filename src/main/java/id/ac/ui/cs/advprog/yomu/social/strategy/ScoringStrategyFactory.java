package id.ac.ui.cs.advprog.yomu.social.strategy;

import id.ac.ui.cs.advprog.yomu.social.model.Tier;
import org.springframework.stereotype.Component;

/**
 * Factory for retrieving the appropriate scoring strategy based on tier.
 */
@Component
public class ScoringStrategyFactory {

    private final BronzeScoringStrategy bronzeStrategy;
    private final SilverScoringStrategy silverStrategy;
    private final GoldScoringStrategy goldStrategy;
    private final DiamondScoringStrategy diamondStrategy;

    public ScoringStrategyFactory(
            BronzeScoringStrategy bronzeStrategy,
            SilverScoringStrategy silverStrategy,
            GoldScoringStrategy goldStrategy,
            DiamondScoringStrategy diamondStrategy) {
        this.bronzeStrategy = bronzeStrategy;
        this.silverStrategy = silverStrategy;
        this.goldStrategy = goldStrategy;
        this.diamondStrategy = diamondStrategy;
    }

    public ScoringStrategy getStrategy(Tier tier) {
        return switch (tier) {
            case BRONZE -> bronzeStrategy;
            case SILVER -> silverStrategy;
            case GOLD -> goldStrategy;
            case DIAMOND -> diamondStrategy;
        };
    }
}
