package id.ac.ui.cs.advprog.yomu.social.strategy;

import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;

import id.ac.ui.cs.advprog.yomu.social.port.ClanMemberValidationPort;

// Tier Diamond: rata-rata tertimbang
// tidak hanya total — konsistensi anggota lebih dihargai
@Component
public class DiamondScoringStrategy implements ScoringStrategy {

    private static final double CONSISTENCY_WEIGHT = 0.6;
    private static final double PEAK_WEIGHT = 0.4;
    private final ClanMemberValidationPort memberValidationPort;

    public DiamondScoringStrategy(ClanMemberValidationPort memberValidationPort) {
        this.memberValidationPort = memberValidationPort;
    }

    @Override
    public int calculateScore(Clan clan, int basePoints) {
        long memberCount = memberValidationPort.countByClanId(clan.getId());
        double averageContribution = clan.getScore() / (double) Math.max(memberCount, 1);
        // rata-rata tertimbang antara konsistensi historis dan performa saat ini
        double weighted = (averageContribution * CONSISTENCY_WEIGHT)
                        + (basePoints * PEAK_WEIGHT);
        return (int) Math.round(weighted);
    }

    @Override
    public Tier getSupportedTier() { return Tier.DIAMOND; }
}
