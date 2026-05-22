package id.ac.ui.cs.advprog.yomu.social.service.modifier;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.social.dto.ClanModifierDTO;
import id.ac.ui.cs.advprog.yomu.social.dto.ModifierSummary;
import id.ac.ui.cs.advprog.yomu.social.mapper.SocialMapper;
import id.ac.ui.cs.advprog.yomu.social.model.ClanModifier;
import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;
import id.ac.ui.cs.advprog.yomu.social.model.ModifierType;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanModifierRepository;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ClanModifierServiceImplTest {

    private static final String CLAN_123 = "clan-123";
    private static final String MSG_DESCRIPTION = "description";

    @Mock
    private ClanModifierRepository modifierRepository;

    @Mock
    private ModifierEvaluatorRegistryPort evaluatorRegistry;

    @Mock
    private SocialMapper socialMapper;

    @InjectMocks
    private ClanModifierServiceImpl modifierService;

    private ClanModifier activeBuff;
    private ClanModifier activeDebuff;
    private ClanModifier futureBuff;
    private ClanModifier expiredBuff;

    @BeforeEach
    void setUp() {
        activeBuff = new ClanModifier();
        activeBuff.setId(1L);
        activeBuff.setClanId(CLAN_123);
        activeBuff.setKey("active-buff");
        activeBuff.setType(ModifierType.BUFF);
        activeBuff.setMultiplier(1.2);
        activeBuff.setActive(true);
        activeBuff.setStartAt(Instant.now().minusSeconds(3600));
        activeBuff.setEndAt(Instant.now().plusSeconds(3600));

        activeDebuff = new ClanModifier();
        activeDebuff.setId(2L);
        activeDebuff.setClanId(CLAN_123);
        activeDebuff.setKey("active-debuff");
        activeDebuff.setType(ModifierType.DEBUFF);
        activeDebuff.setMultiplier(0.8);
        activeDebuff.setActive(true);
        activeDebuff.setStartAt(Instant.now().minusSeconds(3600));
        activeDebuff.setEndAt(Instant.now().plusSeconds(3600));

        futureBuff = new ClanModifier();
        futureBuff.setId(3L);
        futureBuff.setClanId(CLAN_123);
        futureBuff.setKey("future-buff");
        futureBuff.setType(ModifierType.BUFF);
        futureBuff.setMultiplier(1.5);
        futureBuff.setActive(true);
        futureBuff.setStartAt(Instant.now().plusSeconds(3600));
        futureBuff.setEndAt(Instant.now().plusSeconds(7200));

        expiredBuff = new ClanModifier();
        expiredBuff.setId(4L);
        expiredBuff.setClanId(CLAN_123);
        expiredBuff.setKey("expired-buff");
        expiredBuff.setType(ModifierType.BUFF);
        expiredBuff.setMultiplier(1.5);
        expiredBuff.setActive(true);
        expiredBuff.setStartAt(Instant.now().minusSeconds(7200));
        expiredBuff.setEndAt(Instant.now().minusSeconds(3600));
    }

    @Test
    void evaluateModifiers_ShouldCallRegistry() {
        ClanQuizStats stats = new ClanQuizStats();
        doNothing().when(evaluatorRegistry).evaluateAll(CLAN_123, stats);

        assertAll("Verify evaluate modifiers calls registry",
                () -> assertDoesNotThrow(() -> modifierService.evaluateModifiers(CLAN_123, stats),
                        "Should run evaluate modifiers without throwing exception"),
                () -> verify(evaluatorRegistry).evaluateAll(CLAN_123, stats));
    }

    @Test
    void getActiveMultiplier_ShouldAggregateOnlyValidModifiersAndClamp() {
        // Mock active buff, debuff, future buff, and expired buff in the active list
        when(modifierRepository.findByClanIdAndActiveTrue(CLAN_123))
                .thenReturn(List.of(activeBuff, activeDebuff, futureBuff, expiredBuff));

        double multiplier = modifierService.getActiveMultiplier(CLAN_123);

        // Expected valid modifiers are activeBuff (1.2) and activeDebuff (0.8)
        // 1.2 * 0.8 = 0.96
        assertEquals(0.96, multiplier, 0.001, "Multiplier should aggregate only valid modifiers");
    }

    @Test
    void getActiveMultiplier_WhenBuffExceedsMax_ShouldClampToMax() {
        ClanModifier megaBuff = new ClanModifier();
        megaBuff.setClanId(CLAN_123);
        megaBuff.setType(ModifierType.BUFF);
        megaBuff.setMultiplier(12.0);
        megaBuff.setActive(true);
        megaBuff.setStartAt(Instant.now().minusSeconds(60));

        when(modifierRepository.findByClanIdAndActiveTrue(CLAN_123)).thenReturn(List.of(megaBuff));

        double multiplier = modifierService.getActiveMultiplier(CLAN_123);

        // Max limit is 10.0 (SocialConstants.MULTIPLIER_MAX)
        assertEquals(10.0, multiplier, "Multiplier should clamp to maximum limit");
    }

    @Test
    void getActiveMultiplier_WhenDebuffBelowMin_ShouldClampToMin() {
        ClanModifier megaDebuff = new ClanModifier();
        megaDebuff.setClanId(CLAN_123);
        megaDebuff.setType(ModifierType.DEBUFF);
        megaDebuff.setMultiplier(0.1);
        megaDebuff.setActive(true);
        megaDebuff.setStartAt(Instant.now().minusSeconds(60));

        when(modifierRepository.findByClanIdAndActiveTrue(CLAN_123)).thenReturn(List.of(megaDebuff));

        double multiplier = modifierService.getActiveMultiplier(CLAN_123);

        // Min limit is 0.5 (SocialConstants.MULTIPLIER_MIN)
        assertEquals(0.5, multiplier, "Multiplier should clamp to minimum limit");
    }

    @Test
    void getModifierSummary_ShouldSegregateBuffsAndDebuffsAndCalculateMultiplier() {
        when(modifierRepository.findByClanIdAndActiveTrue(CLAN_123)).thenReturn(List.of(activeBuff, activeDebuff));

        ClanModifierDTO buffDTO = new ClanModifierDTO("Active Buff", "1.2", "BUFF", "1h", MSG_DESCRIPTION);
        ClanModifierDTO debuffDTO = new ClanModifierDTO("Active Debuff", "0.8", "DEBUFF", "1h", MSG_DESCRIPTION);

        when(socialMapper.toClanModifierDTO(activeBuff)).thenReturn(buffDTO);
        when(socialMapper.toClanModifierDTO(activeDebuff)).thenReturn(debuffDTO);

        ModifierSummary summary = modifierService.getModifierSummary(CLAN_123);

        assertAll("Verify segregated modifier summary",
                () -> assertNotNull(summary, "ModifierSummary should not be null"),
                () -> assertEquals(1, summary.buffs().size(), "Buffs list size should be 1"),
                () -> assertEquals(buffDTO, summary.buffs().get(0), "Buff DTO should match expected"),
                () -> assertEquals(1, summary.debuffs().size(), "Debuffs list size should be 1"),
                () -> assertEquals(debuffDTO, summary.debuffs().get(0), "Debuff DTO should match expected"),
                () -> assertEquals(0.96, summary.multiplier(), 0.001, "Aggregated multiplier should be 0.96"));
    }

    @Test
    void getModifierSummaries_WhenIdsEmpty_ShouldReturnEmptyMap() {
        assertAll("Verify empty ID results",
                () -> assertTrue(modifierService.getModifierSummaries(null).isEmpty(), "Null IDs list should yield empty map"),
                () -> assertTrue(modifierService.getModifierSummaries(List.of()).isEmpty(), "Empty IDs list should yield empty map"));
    }

    @Test
    void getModifierSummaries_WhenValidIds_ShouldReturnGroupedSummaries() {
        List<String> clanIds = List.of(CLAN_123);
        when(modifierRepository.findByClanIdInAndActiveTrue(clanIds)).thenReturn(List.of(activeBuff, activeDebuff));

        ClanModifierDTO buffDTO = new ClanModifierDTO("Active Buff", "1.2", "BUFF", "1h", MSG_DESCRIPTION);
        ClanModifierDTO debuffDTO = new ClanModifierDTO("Active Debuff", "0.8", "DEBUFF", "1h", MSG_DESCRIPTION);

        when(socialMapper.toClanModifierDTO(activeBuff)).thenReturn(buffDTO);
        when(socialMapper.toClanModifierDTO(activeDebuff)).thenReturn(debuffDTO);

        Map<String, ModifierSummary> summaries = modifierService.getModifierSummaries(clanIds);

        assertAll("Verify batch modifier summaries",
                () -> assertEquals(1, summaries.size(), "Summaries map size should be 1"),
                () -> assertNotNull(summaries.get(CLAN_123), "Summary for clan should not be null"),
                () -> assertEquals(0.96, summaries.get(CLAN_123).multiplier(), 0.001, "Multiplier should aggregate to 0.96"));
    }

    @Test
    void expireStaleModifiers_ShouldCallRepositoryDeactivate() {
        doNothing().when(modifierRepository).deactivateExpired(any(Instant.class));

        assertAll("Verify expire stale modifiers calls repository",
                () -> assertDoesNotThrow(() -> modifierService.expireStaleModifiers(),
                        "Should run expire stale modifiers without throwing exception"),
                () -> verify(modifierRepository).deactivateExpired(any(Instant.class)));
    }
}
