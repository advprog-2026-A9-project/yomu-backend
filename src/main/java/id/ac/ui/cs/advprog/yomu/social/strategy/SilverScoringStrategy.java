package id.ac.ui.cs.advprog.yomu.social.strategy;

import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;

import id.ac.ui.cs.advprog.yomu.social.port.ClanMemberValidationPort;

// Tier Silver: penjumlahan dengan bonus member aktif
// semakin banyak anggota berkontribusi, semakin besar bonus
@Component
public class SilverScoringStrategy implements ScoringStrategy {

    private final ClanMemberValidationPort memberValidationPort;

    public SilverScoringStrategy(ClanMemberValidationPort memberValidationPort) {
        this.memberValidationPort = memberValidationPort;
    }

    @Override
    public int calculateScore(Clan clan, int basePoints) {
        long memberCount = memberValidationPort.countByClanId(clan.getId());
        double bonusMultiplier = 1.0 + (memberCount * 0.05); // +5% per member
        return (int) Math.round(basePoints * bonusMultiplier);
    }

    @Override
    public Tier getSupportedTier() { return Tier.SILVER; }
}
