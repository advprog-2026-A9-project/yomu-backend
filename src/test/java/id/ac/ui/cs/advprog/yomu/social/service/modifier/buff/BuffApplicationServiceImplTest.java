package id.ac.ui.cs.advprog.yomu.social.service.modifier.buff;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.model.ClanModifier;
import id.ac.ui.cs.advprog.yomu.social.model.ModifierType;
import id.ac.ui.cs.advprog.yomu.social.repository.IBuffModifierRepository;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"null", "PMD"})
class BuffApplicationServiceImplTest {

    private static final String CLAN_ID = "clan-123";
    private static final String BUFF_KEY = SocialConstants.DAILY_MISSION_BUFF_KEY;
    private static final String PENALTY_KEY = SocialConstants.LOW_ACCURACY_PENALTY_KEY;
    private static final String UNKNOWN_KEY = "unknown_buff";

    // Assertion messages
    private static final String MSG_EVAL_RESULT = "Value should match";
    private static final String MSG_ACTIVE = "Modifier active state should match";
    private static final String MSG_NOT_NULL = "Object should not be null";

    @Mock
    private IBuffModifierRepository modifierRepository;

    @Mock
    private BuffDefinitionRegistry buffDefinitionRegistry;

    @InjectMocks
    private BuffApplicationServiceImpl buffApplicationService;

    private BuffDefinition dailyMissionBuffDef;
    private BuffDefinition lowAccuracyPenaltyDef;

    @BeforeEach
    void setUp() {
        dailyMissionBuffDef = new DailyMissionBuffDefinition();
        lowAccuracyPenaltyDef = new LowAccuracyPenaltyDefinition();
    }

    // ─── Buff Definitions Tests ──────────────────────────────────────────────

    @Test
    void testDailyMissionBuffDefinition() {
        assertAll("Verify DailyMissionBuffDefinition attributes",
                () -> assertEquals(SocialConstants.DAILY_MISSION_BUFF_KEY, dailyMissionBuffDef.getKey(), MSG_EVAL_RESULT),
                () -> assertEquals(ModifierType.BUFF, dailyMissionBuffDef.getType(), MSG_EVAL_RESULT),
                () -> assertEquals(SocialConstants.DAILY_MISSION_BUFF_MULTIPLIER, dailyMissionBuffDef.getMultiplier(), MSG_EVAL_RESULT),
                () -> assertNotNull(dailyMissionBuffDef.calculateEndAt(Instant.now()), MSG_NOT_NULL)
        );
    }

    @Test
    void testLowAccuracyPenaltyDefinition() {
        Instant now = Instant.now();
        Instant expectedEnd = now.plus(3, ChronoUnit.HOURS);
        assertAll("Verify LowAccuracyPenaltyDefinition attributes",
                () -> assertEquals(SocialConstants.LOW_ACCURACY_PENALTY_KEY, lowAccuracyPenaltyDef.getKey(), MSG_EVAL_RESULT),
                () -> assertEquals(ModifierType.DEBUFF, lowAccuracyPenaltyDef.getType(), MSG_EVAL_RESULT),
                () -> assertEquals(SocialConstants.LOW_ACCURACY_MULTIPLIER, lowAccuracyPenaltyDef.getMultiplier(), MSG_EVAL_RESULT),
                () -> assertEquals(expectedEnd.truncatedTo(ChronoUnit.SECONDS), 
                                    lowAccuracyPenaltyDef.calculateEndAt(now).truncatedTo(ChronoUnit.SECONDS), 
                                    "End time should be 3 hours after start")
        );
    }

    @Test
    void testBuffDefinitionRegistry_FindByKey() {
        BuffDefinitionRegistry registry = new BuffDefinitionRegistry(List.of(dailyMissionBuffDef, lowAccuracyPenaltyDef));

        Optional<BuffDefinition> found = registry.findByKey(BUFF_KEY);
        Optional<BuffDefinition> notFound = registry.findByKey(UNKNOWN_KEY);

        assertAll("Verify BuffDefinitionRegistry lookup behavior",
                () -> assertTrue(found.isPresent(), "Should find registered buff"),
                () -> assertEquals(SocialConstants.DAILY_MISSION_BUFF_KEY, found.get().getKey(), MSG_EVAL_RESULT),
                () -> assertFalse(notFound.isPresent(), "Should not find unregistered buff")
        );
    }

    // ─── BuffApplicationServiceImpl applyBuff Tests ───────────────────────────

    @Test
    void applyBuff_WhenDefinitionNotFound_ShouldDoNothing() {
        when(buffDefinitionRegistry.findByKey(UNKNOWN_KEY)).thenReturn(Optional.empty());

        buffApplicationService.applyBuff(CLAN_ID, UNKNOWN_KEY);

        verify(modifierRepository, never()).saveModifier(any());
    }

    @Test
    void applyBuff_WhenExistingBuffIsActiveAndNotExpired_ShouldSkip() {
        ClanModifier existing = new ClanModifier();
        existing.setClanId(CLAN_ID);
        existing.setKey(BUFF_KEY);
        existing.setActive(true);
        existing.setEndAt(Instant.now().plus(1, ChronoUnit.HOURS));

        when(buffDefinitionRegistry.findByKey(BUFF_KEY)).thenReturn(Optional.of(dailyMissionBuffDef));
        when(modifierRepository.findByClanIdAndKey(CLAN_ID, BUFF_KEY)).thenReturn(Optional.of(existing));

        buffApplicationService.applyBuff(CLAN_ID, BUFF_KEY);

        verify(modifierRepository, never()).saveModifier(any());
    }

    @Test
    void applyBuff_WhenExistingBuffIsActiveButExpired_ShouldReactivateAndSave() {
        ClanModifier existing = new ClanModifier();
        existing.setClanId(CLAN_ID);
        existing.setKey(BUFF_KEY);
        existing.setActive(true);
        existing.setEndAt(Instant.now().minus(1, ChronoUnit.HOURS));

        when(buffDefinitionRegistry.findByKey(BUFF_KEY)).thenReturn(Optional.of(dailyMissionBuffDef));
        when(modifierRepository.findByClanIdAndKey(CLAN_ID, BUFF_KEY)).thenReturn(Optional.of(existing));

        buffApplicationService.applyBuff(CLAN_ID, BUFF_KEY);

        assertAll("Verify expired buff is updated and saved",
                () -> verify(modifierRepository).saveModifier(existing),
                () -> assertTrue(existing.isActive(), MSG_ACTIVE)
        );
    }

    @Test
    void applyBuff_WhenExistingBuffIsInactive_ShouldActivateAndSave() {
        ClanModifier existing = new ClanModifier();
        existing.setClanId(CLAN_ID);
        existing.setKey(BUFF_KEY);
        existing.setActive(false);
        existing.setEndAt(Instant.now().minus(1, ChronoUnit.HOURS));

        when(buffDefinitionRegistry.findByKey(BUFF_KEY)).thenReturn(Optional.of(dailyMissionBuffDef));
        when(modifierRepository.findByClanIdAndKey(CLAN_ID, BUFF_KEY)).thenReturn(Optional.of(existing));

        buffApplicationService.applyBuff(CLAN_ID, BUFF_KEY);

        assertAll("Verify inactive buff is reactivated and saved",
                () -> verify(modifierRepository).saveModifier(existing),
                () -> assertTrue(existing.isActive(), MSG_ACTIVE)
        );
    }

    @Test
    void applyBuff_WhenNoExistingBuff_ShouldCreateNewAndSave() {
        when(buffDefinitionRegistry.findByKey(BUFF_KEY)).thenReturn(Optional.of(dailyMissionBuffDef));
        when(modifierRepository.findByClanIdAndKey(CLAN_ID, BUFF_KEY)).thenReturn(Optional.empty());

        buffApplicationService.applyBuff(CLAN_ID, BUFF_KEY);

        verify(modifierRepository).saveModifier(any(ClanModifier.class));
    }

    // ─── BuffApplicationServiceImpl deactivateBuff Tests ─────────────────────

    @Test
    void deactivateBuff_WhenBuffNotFound_ShouldDoNothing() {
        when(modifierRepository.findByClanIdAndKey(CLAN_ID, BUFF_KEY)).thenReturn(Optional.empty());

        buffApplicationService.deactivateBuff(CLAN_ID, BUFF_KEY);

        verify(modifierRepository, never()).saveModifier(any());
    }

    @Test
    void deactivateBuff_WhenBuffAlreadyInactive_ShouldDoNothing() {
        ClanModifier existing = new ClanModifier();
        existing.setClanId(CLAN_ID);
        existing.setKey(BUFF_KEY);
        existing.setActive(false);

        when(modifierRepository.findByClanIdAndKey(CLAN_ID, BUFF_KEY)).thenReturn(Optional.of(existing));

        buffApplicationService.deactivateBuff(CLAN_ID, BUFF_KEY);

        verify(modifierRepository, never()).saveModifier(any());
    }

    @Test
    void deactivateBuff_WhenBuffIsActive_ShouldDeactivateAndSave() {
        ClanModifier existing = new ClanModifier();
        existing.setClanId(CLAN_ID);
        existing.setKey(BUFF_KEY);
        existing.setActive(true);

        when(modifierRepository.findByClanIdAndKey(CLAN_ID, BUFF_KEY)).thenReturn(Optional.of(existing));

        buffApplicationService.deactivateBuff(CLAN_ID, BUFF_KEY);

        assertAll("Verify active buff is deactivated and saved",
                () -> verify(modifierRepository).saveModifier(existing),
                () -> assertFalse(existing.isActive(), MSG_ACTIVE),
                () -> assertNotNull(existing.getEndAt(), "End time should be populated on deactivation")
        );
    }
}
