package id.ac.ui.cs.advprog.yomu.social.strategy;

import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;

// Tier Gold: penjumlahan dengan diminishing returns
// mencegah clan besar mendominasi terlalu mudah
@Component
public class GoldScoringStrategy implements ScoringStrategy {

    @Override
    public int calculateScore(Clan clan, int basePoints) {
        int currentScore = clan.getScore();
        // semakin tinggi skor, semakin kecil increment — logaritmik
        double factor = 1.0 / (1.0 + Math.log1p(currentScore / 1000.0));
        return (int) Math.round(basePoints * factor);
    }

    @Override
    public Tier getSupportedTier() { return Tier.GOLD; }
}
