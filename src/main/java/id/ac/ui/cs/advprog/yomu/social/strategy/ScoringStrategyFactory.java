package id.ac.ui.cs.advprog.yomu.social.strategy;

import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.social.model.Tier;

/**
 * Factory for retrieving the appropriate scoring strategy based on tier.
 */
@Component
public class ScoringStrategyFactory implements ScoringStrategyResolver {

    private final java.util.Map<Tier, ScoringStrategy> strategyMap;

    public ScoringStrategyFactory(java.util.List<ScoringStrategy> strategies) {
        strategyMap = strategies.stream()
                .collect(java.util.stream.Collectors.toMap(
                        ScoringStrategy::getSupportedTier,
                        strategy -> strategy
                ));
    }

    @Override
    public ScoringStrategy getStrategy(Tier tier) {
        ScoringStrategy strategy = strategyMap.get(tier);
        if (strategy == null) {
            throw new IllegalArgumentException("No scoring strategy found for tier: " + tier);
        }
        return strategy;
    }
}
