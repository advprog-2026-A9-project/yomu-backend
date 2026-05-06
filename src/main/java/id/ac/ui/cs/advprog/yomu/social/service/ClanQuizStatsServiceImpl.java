package id.ac.ui.cs.advprog.yomu.social.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanQuizStatsRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClanQuizStatsServiceImpl implements ClanQuizStatsService {

    private final ClanQuizStatsRepository statsRepository;

    @Override
    @Transactional
    public ClanQuizStats recordQuizResult(String clanId, int correctAnswers, int totalQuestions, int score) {
        final String validClanId = Objects.requireNonNull(clanId);
        ClanQuizStats stats = statsRepository.findById(validClanId).orElseGet(() -> {
            ClanQuizStats created = new ClanQuizStats();
            created.setClanId(validClanId);
            return created;
        });

        stats.setTotalQuizAttempts(stats.getTotalQuizAttempts() + 1);
        stats.setTotalCorrectAnswers(stats.getTotalCorrectAnswers() + correctAnswers);
        stats.setTotalQuestions(stats.getTotalQuestions() + totalQuestions);
        stats.setTotalScore(stats.getTotalScore() + score);

        return statsRepository.save(stats);
    }

    @Override
    public double getAccuracyRatio(ClanQuizStats stats) {
        if (stats.getTotalQuestions() <= 0) {
            return 1.0d;
        }

        return (double) stats.getTotalCorrectAnswers() / (double) stats.getTotalQuestions();
    }

    @Override
    @Transactional
    public void resetSeasonStats() {
        statsRepository.deleteAllInBatch();
    }
}
