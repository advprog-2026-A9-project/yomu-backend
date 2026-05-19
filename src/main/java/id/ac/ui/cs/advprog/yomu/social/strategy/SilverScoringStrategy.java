package id.ac.ui.cs.advprog.yomu.social.strategy;

import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;

import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanQuizStatsRepository;

// Tier Silver: penjumlahan dengan bonus member aktif
// semakin banyak kontribusi dari anggota, semakin besar bonus
@SuppressWarnings("null")
@Component
public class SilverScoringStrategy implements ScoringStrategy {

    private final ClanQuizStatsRepository quizStatsRepository;

    public SilverScoringStrategy(ClanQuizStatsRepository quizStatsRepository) {
        this.quizStatsRepository = quizStatsRepository;
    }

    @Override
    public int calculateScore(Clan clan, int basePoints) {
        ClanQuizStats stats = quizStatsRepository.findById(clan.getId()).orElse(new ClanQuizStats());
        long contributionCount = stats.getTotalQuizAttempts();
        double bonusMultiplier = 1.0 + (contributionCount * 0.05); // +5% per kontribusi
        return (int) Math.round(basePoints * bonusMultiplier);
    }

    @Override
    public Tier getSupportedTier() {
        return Tier.SILVER;
    }
}
