package id.ac.ui.cs.advprog.yomu.social.strategy;

import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;

// Tier Bronze: penjumlahan total langsung — paling sederhana
@Component
public class BronzeScoringStrategy implements ScoringStrategy {

    @Override
    public int calculateScore(Clan clan, int basePoints) {
        return basePoints; // langsung tambah apa adanya
    }

    @Override
    public Tier getSupportedTier() { return Tier.BRONZE; }
}
