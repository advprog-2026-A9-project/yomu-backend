package id.ac.ui.cs.advprog.yomu.social.strategy;

import id.ac.ui.cs.advprog.yomu.social.model.Tier;

public interface ScoringStrategyResolver {
    ScoringStrategy getStrategy(Tier tier);
}