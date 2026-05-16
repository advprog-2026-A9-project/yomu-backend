package id.ac.ui.cs.advprog.yomu.social.strategy;

import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.social.model.Tier;

/**
 * Factory for retrieving the appropriate scoring strategy based on tier.
 */
@Component
public class ScoringStrategyFactory implements ScoringStrategyResolver {

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

    @Override
    public ScoringStrategy getStrategy(Tier tier) {
        return switch (tier) {
            case BRONZE -> bronzeStrategy;
            case SILVER -> silverStrategy;
            case GOLD -> goldStrategy;
            case DIAMOND -> diamondStrategy;
        };
    }
}
