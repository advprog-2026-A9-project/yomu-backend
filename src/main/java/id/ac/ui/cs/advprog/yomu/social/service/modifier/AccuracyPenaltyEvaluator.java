package id.ac.ui.cs.advprog.yomu.social.service.modifier;

import java.util.Optional;

import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;
import id.ac.ui.cs.advprog.yomu.social.service.score.ClanQuizStatsService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccuracyPenaltyEvaluator implements ModifierEvaluator {

    private static final int MIN_REQUIRED_HISTORY_SIZE = 10;

    private final ClanQuizStatsService statsService;

    @Override
    public String getKey() {
        return SocialConstants.LOW_ACCURACY_PENALTY_KEY;
    }

    @Override
    public Optional<String> evaluate(String clanId, ClanQuizStats stats) {
        String history = stats.getRollingQuizHistory();
        if (history == null || history.trim().isEmpty()) {
            return Optional.empty();
        }

        String[] parts = history.split(",");
        if (parts.length < MIN_REQUIRED_HISTORY_SIZE) {
            return Optional.empty();
        }

        double accuracyRatio = statsService.getAccuracyRatio(stats);
        if (accuracyRatio < SocialConstants.LOW_ACCURACY_THRESHOLD) {
            return Optional.of(getKey());
        }
        return Optional.empty();
    }
}