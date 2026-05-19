package id.ac.ui.cs.advprog.yomu.social.strategy;

import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;

/**
 * Strategy interface for calculating clan scores based on tier level.
 * Different tiers may use different scoring algorithms.
 */
public interface ScoringStrategy {
    /**
     * Calculate the score for a clan based on tier-specific algorithm.
     * @param clan the clan to calculate score for
     * @param basePoints accumulated base points from activities
     * @return calculated score
     */
    int calculateScore(Clan clan, int basePoints);

    /**
     * Get the tier that this scoring strategy supports.
     * @return supported tier
     */
    Tier getSupportedTier();
}
