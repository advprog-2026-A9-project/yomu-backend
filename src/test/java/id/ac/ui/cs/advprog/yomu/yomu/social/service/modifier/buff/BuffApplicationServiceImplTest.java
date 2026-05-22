package id.ac.ui.cs.advprog.yomu.social.service.modifier.buff;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
@SuppressWarnings("null")
class BuffApplicationServiceImplTest {

    @Mock
    private IBuffModifierRepository modifierRepository;

    @Mock
    private BuffDefinitionRegistry buffDefinitionRegistry;

    @InjectMocks
    private BuffApplicationServiceImpl buffApplicationService;

    private String clanId;
    private String buffKey;
    private DailyMissionBuffDefinition dailyMissionBuffDef;
    private LowAccuracyPenaltyDefinition lowAccuracyPenaltyDef;

    @BeforeEach
    void setUp() {
        clanId = "clan-123";
        buffKey = SocialConstants.DAILY_MISSION_BUFF_KEY;
        dailyMissionBuffDef = new DailyMissionBuffDefinition();
        lowAccuracyPenaltyDef = new LowAccuracyPenaltyDefinition();
    }

    @Test
    void applyBuff_WhenDefinitionNotFound_ShouldDoNothing() {
        when(buffDefinitionRegistry.findByKey(buffKey)).thenReturn(Optional.empty());

        assertAll("Verify apply buff is skipped when definition not found",
                () -> assertDoesNotThrow(() -> buffApplicationService.applyBuff(clanId, buffKey),
                        "Should skip applying buff when definition is not found"),
                () -> verify(modifierRepository, never()).findByClanIdAndKey(any(), any()),
                () -> verify(modifierRepository, never()).saveModifier(any()));
    }

    @Test
    void applyBuff_WhenBuffAlreadyActiveAndNotExpired_ShouldSkip() {
        ClanModifier existing = new ClanModifier();
        existing.setActive(true);
        existing.setEndAt(Instant.now().plusSeconds(3600));

        when(buffDefinitionRegistry.findByKey(buffKey)).thenReturn(Optional.of(dailyMissionBuffDef));
        when(modifierRepository.findByClanIdAndKey(clanId, buffKey)).thenReturn(Optional.of(existing));

        assertAll("Verify apply buff is skipped when already active and not expired",
                () -> assertDoesNotThrow(() -> buffApplicationService.applyBuff(clanId, buffKey),
                        "Should skip applying buff when it is already active and not expired"),
                () -> verify(modifierRepository, never()).saveModifier(any()));
    }

    @Test
    void applyBuff_WhenBuffActiveButExpired_ShouldReactivateAndSave() {
        ClanModifier existing = new ClanModifier();
        existing.setActive(true);
        existing.setEndAt(Instant.now().minusSeconds(3600));

        when(buffDefinitionRegistry.findByKey(buffKey)).thenReturn(Optional.of(dailyMissionBuffDef));
        when(modifierRepository.findByClanIdAndKey(clanId, buffKey)).thenReturn(Optional.of(existing));

        buffApplicationService.applyBuff(clanId, buffKey);

        assertAll("Verify reactivated buff properties",
                () -> assertTrue(existing.isActive(), "Buff should be reactivated"),
                () -> assertNotNull(existing.getStartAt(), "Start time should be set"),
                () -> assertNotNull(existing.getEndAt(), "End time should be set"),
                () -> verify(modifierRepository).saveModifier(existing));
    }

    @Test
    void applyBuff_WhenBuffNotPresent_ShouldCreateAndSave() {
        when(buffDefinitionRegistry.findByKey(buffKey)).thenReturn(Optional.of(dailyMissionBuffDef));
        when(modifierRepository.findByClanIdAndKey(clanId, buffKey)).thenReturn(Optional.empty());

        assertAll("Verify new buff is created and saved",
                () -> assertDoesNotThrow(() -> buffApplicationService.applyBuff(clanId, buffKey),
                        "Should successfully apply and save new buff"),
                () -> verify(modifierRepository).saveModifier(any(ClanModifier.class)));
    }

    @Test
    void deactivateBuff_WhenNotFound_ShouldDoNothing() {
        when(modifierRepository.findByClanIdAndKey(clanId, buffKey)).thenReturn(Optional.empty());

        assertAll("Verify deactivation is skipped when buff not found",
                () -> assertDoesNotThrow(() -> buffApplicationService.deactivateBuff(clanId, buffKey),
                        "Should skip deactivation when buff is not found"),
                () -> verify(modifierRepository, never()).saveModifier(any()));
    }

    @Test
    void deactivateBuff_WhenAlreadyInactive_ShouldSkip() {
        ClanModifier existing = new ClanModifier();
        existing.setActive(false);

        when(modifierRepository.findByClanIdAndKey(clanId, buffKey)).thenReturn(Optional.of(existing));

        assertAll("Verify deactivation is skipped when buff is already inactive",
                () -> assertDoesNotThrow(() -> buffApplicationService.deactivateBuff(clanId, buffKey),
                        "Should skip deactivation when buff is already inactive"),
                () -> verify(modifierRepository, never()).saveModifier(any()));
    }

    @Test
    void deactivateBuff_WhenActive_ShouldDeactivateAndSave() {
        ClanModifier existing = new ClanModifier();
        existing.setActive(true);

        when(modifierRepository.findByClanIdAndKey(clanId, buffKey)).thenReturn(Optional.of(existing));

        buffApplicationService.deactivateBuff(clanId, buffKey);

        assertAll("Verify deactivated buff state",
                () -> assertFalse(existing.isActive(), "Buff should be deactivated"),
                () -> assertNotNull(existing.getEndAt(), "End time should be set"),
                () -> verify(modifierRepository).saveModifier(existing));
    }

    @Test
    void buffDefinitionRegistry_ShouldFindKey() {
        BuffDefinitionRegistry registry = new BuffDefinitionRegistry(List.of(dailyMissionBuffDef, lowAccuracyPenaltyDef));

        Optional<BuffDefinition> result = registry.findByKey(SocialConstants.DAILY_MISSION_BUFF_KEY);
        Optional<BuffDefinition> missing = registry.findByKey("invalid-key");

        assertAll("Verify registry lookup",
                () -> assertTrue(result.isPresent(), "Registry should find daily mission buff"),
                () -> assertEquals(dailyMissionBuffDef, result.get(), "Registry should return correct buff instance"),
                () -> assertFalse(missing.isPresent(), "Registry should not find invalid key"));
    }

    @Test
    void dailyMissionBuffDefinition_VerifyPropertiesAndEndAtCalculation() {
        Instant start = Instant.parse("2026-05-22T10:00:00Z");
        Instant expectedEnd = start.atZone(ZoneId.systemDefault())
                .toLocalDate()
                .atTime(23, 59, 59)
                .atZone(ZoneId.systemDefault())
                .toInstant();

        assertAll("Verify DailyMissionBuff properties",
                () -> assertEquals(SocialConstants.DAILY_MISSION_BUFF_KEY, dailyMissionBuffDef.getKey(), "Key should match constant"),
                () -> assertEquals(ModifierType.BUFF, dailyMissionBuffDef.getType(), "Type should be BUFF"),
                () -> assertEquals(SocialConstants.DAILY_MISSION_BUFF_MULTIPLIER, dailyMissionBuffDef.getMultiplier(), "Multiplier should match constant"),
                () -> assertEquals(expectedEnd, dailyMissionBuffDef.calculateEndAt(start), "End at calculation should match expected end of day"));
    }

    @Test
    void lowAccuracyPenaltyDefinition_VerifyPropertiesAndEndAtCalculation() {
        Instant start = Instant.parse("2026-05-22T10:00:00Z");
        Instant expectedEnd = start.plusSeconds(3 * 3600);

        assertAll("Verify LowAccuracyPenalty properties",
                () -> assertEquals(SocialConstants.LOW_ACCURACY_PENALTY_KEY, lowAccuracyPenaltyDef.getKey(), "Key should match constant"),
                () -> assertEquals(ModifierType.DEBUFF, lowAccuracyPenaltyDef.getType(), "Type should be DEBUFF"),
                () -> assertEquals(SocialConstants.LOW_ACCURACY_MULTIPLIER, lowAccuracyPenaltyDef.getMultiplier(), "Multiplier should match constant"),
                () -> assertEquals(expectedEnd, lowAccuracyPenaltyDef.calculateEndAt(start), "End at calculation should match start + 3 hours"));
    }

    @Test
    void buffDefinition_DefaultInterfaceMethods() {
        BuffDefinition customDef = new BuffDefinition() {
            @Override
            public String getKey() {
                return "custom";
            }

            @Override
            public ModifierType getType() {
                return ModifierType.BUFF;
            }

            @Override
            public double getMultiplier() {
                return 1.1;
            }
        };

        ClanModifier modifier = customDef.createModifier("clan-x");

        assertAll("Verify default custom buff definition interface behavior",
                () -> assertNull(customDef.calculateEndAt(Instant.now()), "Default calculateEndAt should return null"),
                () -> assertEquals("clan-x", modifier.getClanId(), "Modifier clan ID should match"),
                () -> assertEquals("custom", modifier.getKey(), "Modifier key should match"),
                () -> assertEquals(ModifierType.BUFF, modifier.getType(), "Modifier type should be BUFF"),
                () -> assertEquals(1.1, modifier.getMultiplier(), "Modifier multiplier should be 1.1"),
                () -> assertTrue(modifier.isActive(), "Modifier should be active"),
                () -> assertNull(modifier.getEndAt(), "Modifier end time should be null"));
    }
}
