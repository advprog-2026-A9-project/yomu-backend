package id.ac.ui.cs.advprog.yomu.social.service.score;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanQuizStatsRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClanQuizStatsServiceImpl implements ClanQuizStatsService {

    private static final int MAX_ROLLING_HISTORY = 10;
    private static final int ROLLING_HISTORY_OFFSET = 9;
    private static final int SPLIT_PARTS_COUNT = 2;

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

        // Update aggregated totals
        stats.setTotalQuizAttempts(stats.getTotalQuizAttempts() + 1);
        stats.setTotalCorrectAnswers(stats.getTotalCorrectAnswers() + correctAnswers);
        stats.setTotalQuestions(stats.getTotalQuestions() + totalQuestions);
        stats.setTotalScore(stats.getTotalScore() + score);

        // Update rolling window history (maximum 10 entries)
        String history = stats.getRollingQuizHistory();
        if (history == null) {
            history = "";
        }
        history = history.trim();

        String newEntry = correctAnswers + "/" + totalQuestions;
        if (history.isEmpty()) {
            history = newEntry;
        } else {
            String[] parts = history.split(",");
            if (parts.length >= MAX_ROLLING_HISTORY) {
                StringBuilder sb = new StringBuilder();
                for (int i = parts.length - ROLLING_HISTORY_OFFSET; i < parts.length; i++) {
                    sb.append(parts[i]).append(",");
                }
                sb.append(newEntry);
                history = sb.toString();
            } else {
                history = history + "," + newEntry;
            }
        }
        stats.setRollingQuizHistory(history);

        return statsRepository.save(stats);
    }

    @Override
    public double getAccuracyRatio(ClanQuizStats stats) {
        String history = stats.getRollingQuizHistory();
        if (history == null || history.trim().isEmpty()) {
            if (stats.getTotalQuestions() <= 0) {
                return 1.0d;
            }
            return (double) stats.getTotalCorrectAnswers() / (double) stats.getTotalQuestions();
        }

        String[] parts = history.split(",");
        long totalCorrect = 0;
        long totalQuestions = 0;

        for (String part : parts) {
            String[] split = part.split("/");
            if (split.length == SPLIT_PARTS_COUNT) {
                try {
                    totalCorrect += Long.parseLong(split[0]);
                    totalQuestions += Long.parseLong(split[1]);
                } catch (NumberFormatException ignored) {}
            }
        }

        if (totalQuestions <= 0) {
            return 1.0d;
        }

        return (double) totalCorrect / (double) totalQuestions;
    }

    @Override
    @Transactional
    public void resetSeasonStats() {
        statsRepository.deleteAllInBatch();
    }
}
