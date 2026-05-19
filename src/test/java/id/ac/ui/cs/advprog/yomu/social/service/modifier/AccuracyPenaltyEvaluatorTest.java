package id.ac.ui.cs.advprog.yomu.social.service.modifier;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;
import id.ac.ui.cs.advprog.yomu.social.service.score.ClanQuizStatsService;

@ExtendWith(MockitoExtension.class)
class AccuracyPenaltyEvaluatorTest {

        private static final String CLAN_ID = "clan-1";

        @Mock
        private ClanQuizStatsService statsService;

        @InjectMocks
        private AccuracyPenaltyEvaluator evaluator;

        @Test
        void evaluate_WhenSeasonAccuracyBelowThresholdAndHistoryComplete_ShouldReturnPenaltyKey() {
                ClanQuizStats stats = new ClanQuizStats();
                stats.setRollingQuizHistory("1/2,1/2,1/2,1/2,1/2,1/2,1/2,1/2,1/2,1/2");
                when(statsService.getAccuracyRatio(stats)).thenReturn(0.4d);

                Optional<String> result = evaluator.evaluate(CLAN_ID, stats);

                assertEquals(Optional.of(SocialConstants.LOW_ACCURACY_PENALTY_KEY), result,
                        "Should return penalty key in Optional");
        }

        @Test
        void evaluate_WhenSeasonAccuracyAboveThresholdAndHistoryComplete_ShouldReturnEmpty() {
                ClanQuizStats stats = new ClanQuizStats();
                stats.setRollingQuizHistory("1/2,1/2,1/2,1/2,1/2,1/2,1/2,1/2,1/2,1/2");
                when(statsService.getAccuracyRatio(stats)).thenReturn(0.75d);

                Optional<String> result = evaluator.evaluate(CLAN_ID, stats);

                assertTrue(result.isEmpty(), "Result should be empty");
        }

        @Test
        void evaluate_WhenHistoryLessThanTen_ShouldReturnEmptyEvenIfAccuracyLow() {
                ClanQuizStats stats = new ClanQuizStats();
                stats.setRollingQuizHistory("1/5,1/5");

                Optional<String> result = evaluator.evaluate(CLAN_ID, stats);

                assertTrue(result.isEmpty(), "Should not apply penalty when history length is less than 10");
        }
}
