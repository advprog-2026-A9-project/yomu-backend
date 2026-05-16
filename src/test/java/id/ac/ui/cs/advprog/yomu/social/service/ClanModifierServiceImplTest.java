package id.ac.ui.cs.advprog.yomu.social.service;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import id.ac.ui.cs.advprog.yomu.social.service.modifier.ModifierEvaluatorRegistryPort;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class ClanModifierServiceImplTest {

    private static final String CLAN_ID = "clan-1";

    @Mock
    private ClanModifierRepository modifierRepository;

    @Mock
    private ModifierEvaluatorRegistryPort evaluatorRegistry;

    @InjectMocks
    private ClanModifierServiceImpl modifierService;

    @Test
    void evaluateModifiers_ShouldDelegateToRegistry() {
        ClanQuizStats stats = new ClanQuizStats();

        modifierService.evaluateModifiers(CLAN_ID, stats);

        verify(evaluatorRegistry).evaluateAll(CLAN_ID, stats);
    }

    @Test
    void getActiveMultiplier_WhenExpiredAndFutureModifiers_ShouldClampMinimum() {
        Instant now = Instant.now();

        ClanModifier active = new ClanModifier();
        active.setClanId(CLAN_ID);
        active.setKey("ACTIVE");
        active.setType(ModifierType.BUFF);
        active.setMultiplier(0.2d);
        active.setActive(true);
        active.setStartAt(now.minusSeconds(60));

        ClanModifier future = new ClanModifier();
        future.setClanId(CLAN_ID);
        future.setKey("FUTURE");
        future.setType(ModifierType.BUFF);
        future.setMultiplier(2.0d);
        future.setActive(true);
        future.setStartAt(now.plusSeconds(60));

        ClanModifier expired = new ClanModifier();
        expired.setClanId(CLAN_ID);
        expired.setKey("EXPIRED");
        expired.setType(ModifierType.BUFF);
        expired.setMultiplier(2.0d);
        expired.setActive(true);
        expired.setStartAt(now.minusSeconds(120));
        expired.setEndAt(now.minusSeconds(30));

        when(modifierRepository.findByClanIdAndActiveTrue(CLAN_ID))
                .thenReturn(List.of(active, future, expired));

        double multiplier = modifierService.getActiveMultiplier(CLAN_ID);

        assertAll("Verify multiplier calculation and cleanup",
                () -> assertEquals(SocialConstants.MULTIPLIER_MIN, multiplier, 0.0001d,
                        "Multiplier should clamp to minimum"),
                () -> verify(modifierRepository).save(expired));
    }

    @Test
    void clearSeasonModifiers_WhenActiveExists_ShouldDeactivateAll() {
        ClanModifier activeOne = new ClanModifier();
        activeOne.setActive(true);
        ClanModifier activeTwo = new ClanModifier();
        activeTwo.setActive(true);
        ClanModifier inactive = new ClanModifier();
        inactive.setActive(false);

        when(modifierRepository.findAll()).thenReturn(List.of(activeOne, activeTwo, inactive));

        modifierService.clearSeasonModifiers();

        assertAll("Verify all active modifiers are deactivated and saved",
                () -> assertFalse(activeOne.isActive(), "First modifier should be deactivated"),
                () -> assertFalse(activeTwo.isActive(), "Second modifier should be deactivated"),
                () -> verify(modifierRepository).saveAll(List.of(activeOne, activeTwo)));
    }
}
