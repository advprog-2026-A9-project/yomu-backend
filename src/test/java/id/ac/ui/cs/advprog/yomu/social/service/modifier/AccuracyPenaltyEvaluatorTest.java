package id.ac.ui.cs.advprog.yomu.social.service.modifier;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.model.ClanModifier;
import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;
import id.ac.ui.cs.advprog.yomu.social.model.ModifierType;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanModifierRepository;
import id.ac.ui.cs.advprog.yomu.social.service.ClanQuizStatsService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AccuracyPenaltyEvaluatorTest {

        private static final String CLAN_ID = "clan-1";

        @Mock
        private ClanModifierRepository modifierRepository;

        @Mock
        private ClanQuizStatsService statsService;

        @InjectMocks
        private AccuracyPenaltyEvaluator evaluator;

        @Test
        void evaluate_WhenSeasonAccuracyBelowThreshold_ShouldActivatePenalty() {
                ClanQuizStats stats = new ClanQuizStats();
                stats.setTotalCorrectAnswers(20);
                stats.setTotalQuestions(50);

                when(statsService.getAccuracyRatio(stats)).thenReturn(0.4d);
                when(modifierRepository.findByClanIdAndKey(CLAN_ID, SocialConstants.LOW_ACCURACY_PENALTY_KEY))
                                .thenReturn(Optional.empty());

                ArgumentCaptor<ClanModifier> captor = ArgumentCaptor.forClass(ClanModifier.class);
                evaluator.evaluate(CLAN_ID, stats);

                assertAll("Verify saved penalty modifier properties",
                                () -> verify(modifierRepository).save(captor.capture()),
                                () -> {
                                        ClanModifier saved = captor.getValue();
                                        assertAll("Saved modifier details",
                                                        () -> assertEquals(CLAN_ID, saved.getClanId(),
                                                                        "Clan ID should match"),
                                                        () -> assertEquals(SocialConstants.LOW_ACCURACY_PENALTY_KEY,
                                                                        saved.getKey(),
                                                                        "Key should be LOW_ACCURACY_PENALTY"),
                                                        () -> assertEquals(ModifierType.DEBUFF, saved.getType(),
                                                                        "Type should be DEBUFF"),
                                                        () -> assertEquals(SocialConstants.LOW_ACCURACY_MULTIPLIER,
                                                                        saved.getMultiplier(),
                                                                        0.0001d,
                                                                        "Multiplier should match low accuracy multiplier"),
                                                        () -> assertTrue(saved.isActive(), "Modifier should be active"),
                                                        () -> assertNull(saved.getEndAt(),
                                                                        "EndAt should be null for season-long penalty"));
                                });
        }

        @Test
        void evaluate_WhenSeasonAccuracyRecovered_ShouldDeactivatePenalty() {
                ClanQuizStats stats = new ClanQuizStats();

                ClanModifier existing = new ClanModifier();
                existing.setClanId(CLAN_ID);
                existing.setKey(SocialConstants.LOW_ACCURACY_PENALTY_KEY);
                existing.setType(ModifierType.DEBUFF);
                existing.setMultiplier(SocialConstants.LOW_ACCURACY_MULTIPLIER);
                existing.setActive(true);
                existing.setStartAt(Instant.now().minusSeconds(300));

                when(statsService.getAccuracyRatio(stats)).thenReturn(0.75d);
                when(modifierRepository.findByClanIdAndKey(CLAN_ID, SocialConstants.LOW_ACCURACY_PENALTY_KEY))
                                .thenReturn(Optional.of(existing));

                evaluator.evaluate(CLAN_ID, stats);

                assertAll("Verify penalty deactivation",
                                () -> assertFalse(existing.isActive(), "Penalty should be deactivated"),
                                () -> verify(modifierRepository).save(existing));
        }
}
