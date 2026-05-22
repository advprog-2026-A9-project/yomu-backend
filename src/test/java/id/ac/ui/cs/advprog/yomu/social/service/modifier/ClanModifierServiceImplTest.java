package id.ac.ui.cs.advprog.yomu.social.service.modifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.social.dto.ClanModifierDTO;
import id.ac.ui.cs.advprog.yomu.social.dto.ModifierSummary;
import id.ac.ui.cs.advprog.yomu.social.mapper.SocialMapper;
import id.ac.ui.cs.advprog.yomu.social.model.ClanModifier;
import id.ac.ui.cs.advprog.yomu.social.model.ModifierType;
import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanModifierRepository;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ClanModifierServiceImplTest {

    private static final String CLAN_1 = "clan-1";
    private static final String CLAN_2 = "clan-2";
    private static final String BUFF_KEY = "daily_mission_buff";
    private static final String DEBUFF_KEY = "low_accuracy_penalty";

    // Assertion messages
    private static final String MSG_MULTIPLIER = "Multiplier should match";
    private static final String MSG_NOT_NULL = "Result should not be null";
    private static final String MSG_SIZE = "List/Map size should match";

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
    private ClanModifierDTO buffDTO;
    private ClanModifierDTO debuffDTO;

    @BeforeEach
    void setUp() {
        activeBuff = new ClanModifier();
        activeBuff.setClanId(CLAN_1);
        activeBuff.setKey(BUFF_KEY);
        activeBuff.setType(ModifierType.BUFF);
        activeBuff.setMultiplier(1.2);
        activeBuff.setActive(true);
        activeBuff.setStartAt(Instant.now().minus(1, ChronoUnit.HOURS));
        activeBuff.setEndAt(Instant.now().plus(1, ChronoUnit.HOURS));

        activeDebuff = new ClanModifier();
        activeDebuff.setClanId(CLAN_1);
        activeDebuff.setKey(DEBUFF_KEY);
        activeDebuff.setType(ModifierType.DEBUFF);
        activeDebuff.setMultiplier(0.8);
        activeDebuff.setActive(true);
        activeDebuff.setStartAt(Instant.now().minus(1, ChronoUnit.HOURS));
        activeDebuff.setEndAt(Instant.now().plus(1, ChronoUnit.HOURS));

        buffDTO = new ClanModifierDTO("Daily Mission Buff", "x1.20", "buff", "Active", "Description");
        debuffDTO = new ClanModifierDTO("Low Accuracy Penalty", "x0.80", "debuff", "Active", "Description");
    }

    @Test
    void evaluateModifiers_ShouldDelegateToRegistry() {
        ClanQuizStats stats = new ClanQuizStats();
        modifierService.evaluateModifiers(CLAN_1, stats);
        verify(evaluatorRegistry).evaluateAll(CLAN_1, stats);
    }

    @Test
    void getActiveMultiplier_WhenNoModifiers_ShouldReturnOne() {
        when(modifierRepository.findByClanIdAndActiveTrue(CLAN_1)).thenReturn(List.of());

        double result = modifierService.getActiveMultiplier(CLAN_1);

        assertEquals(1.0, result, MSG_MULTIPLIER);
    }

    @Test
    void getActiveMultiplier_WithActiveModifiers_ShouldCalculateCombinedMultiplier() {
        when(modifierRepository.findByClanIdAndActiveTrue(CLAN_1)).thenReturn(List.of(activeBuff, activeDebuff));

        double result = modifierService.getActiveMultiplier(CLAN_1);

        // 1.2 * 0.8 = 0.96
        assertEquals(0.96, result, MSG_MULTIPLIER);
    }

    @Test
    void getActiveMultiplier_WhenModifierNotStartedYet_ShouldIgnore() {
        activeBuff.setStartAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        when(modifierRepository.findByClanIdAndActiveTrue(CLAN_1)).thenReturn(List.of(activeBuff));

        double result = modifierService.getActiveMultiplier(CLAN_1);

        assertEquals(1.0, result, MSG_MULTIPLIER);
    }

    @Test
    void getActiveMultiplier_WhenModifierExpired_ShouldIgnore() {
        activeBuff.setEndAt(Instant.now().minus(10, ChronoUnit.MINUTES));
        when(modifierRepository.findByClanIdAndActiveTrue(CLAN_1)).thenReturn(List.of(activeBuff));

        double result = modifierService.getActiveMultiplier(CLAN_1);

        assertEquals(1.0, result, MSG_MULTIPLIER);
    }

    @Test
    void getActiveMultiplier_WhenBelowMinClamp_ShouldClampToMin() {
        activeDebuff.setMultiplier(0.1); // 0.1 < 0.5 (min clamp)
        when(modifierRepository.findByClanIdAndActiveTrue(CLAN_1)).thenReturn(List.of(activeDebuff));

        double result = modifierService.getActiveMultiplier(CLAN_1);

        assertEquals(0.5, result, MSG_MULTIPLIER);
    }

    @Test
    void getActiveMultiplier_WhenAboveMaxClamp_ShouldClampToMax() {
        activeBuff.setMultiplier(15.0); // 15.0 > 10.0 (max clamp)
        when(modifierRepository.findByClanIdAndActiveTrue(CLAN_1)).thenReturn(List.of(activeBuff));

        double result = modifierService.getActiveMultiplier(CLAN_1);

        assertEquals(10.0, result, MSG_MULTIPLIER);
    }

    @Test
    void getModifierSummary_ShouldReturnMappedBuffsAndDebuffs() {
        when(modifierRepository.findByClanIdAndActiveTrue(CLAN_1)).thenReturn(List.of(activeBuff, activeDebuff));
        when(socialMapper.toClanModifierDTO(activeBuff)).thenReturn(buffDTO);
        when(socialMapper.toClanModifierDTO(activeDebuff)).thenReturn(debuffDTO);

        ModifierSummary summary = modifierService.getModifierSummary(CLAN_1);

        assertAll("Verify ModifierSummary results",
                () -> assertNotNull(summary, MSG_NOT_NULL),
                () -> assertEquals(1, summary.buffs().size(), MSG_SIZE),
                () -> assertEquals(1, summary.debuffs().size(), MSG_SIZE),
                () -> assertEquals(0.96, summary.multiplier(), MSG_MULTIPLIER)
        );
    }

    @Test
    void getModifierSummaries_WhenClanIdsNullOrEmpty_ShouldReturnEmptyMap() {
        Map<String, ModifierSummary> result1 = modifierService.getModifierSummaries(null);
        Map<String, ModifierSummary> result2 = modifierService.getModifierSummaries(List.of());

        assertAll("Verify empty clan ids handling",
                () -> assertNotNull(result1, MSG_NOT_NULL),
                () -> assertTrue(result1.isEmpty(), MSG_SIZE),
                () -> assertNotNull(result2, MSG_NOT_NULL),
                () -> assertTrue(result2.isEmpty(), MSG_SIZE)
        );
    }

    @Test
    void getModifierSummaries_WithMultipleClans_ShouldGroupAndReturnModifierSummaries() {
        ClanModifier clan2Buff = new ClanModifier();
        clan2Buff.setClanId(CLAN_2);
        clan2Buff.setKey(BUFF_KEY);
        clan2Buff.setType(ModifierType.BUFF);
        clan2Buff.setMultiplier(1.2);
        clan2Buff.setActive(true);
        clan2Buff.setStartAt(Instant.now().minus(1, ChronoUnit.HOURS));
        clan2Buff.setEndAt(Instant.now().plus(1, ChronoUnit.HOURS));

        when(modifierRepository.findByClanIdInAndActiveTrue(List.of(CLAN_1, CLAN_2)))
                .thenReturn(List.of(activeBuff, activeDebuff, clan2Buff));
        when(socialMapper.toClanModifierDTO(activeBuff)).thenReturn(buffDTO);
        when(socialMapper.toClanModifierDTO(activeDebuff)).thenReturn(debuffDTO);
        when(socialMapper.toClanModifierDTO(clan2Buff)).thenReturn(buffDTO);

        Map<String, ModifierSummary> summaries = modifierService.getModifierSummaries(List.of(CLAN_1, CLAN_2));

        assertAll("Verify grouped modifier summaries",
                () -> assertNotNull(summaries, MSG_NOT_NULL),
                () -> assertEquals(2, summaries.size(), MSG_SIZE),
                () -> assertEquals(0.96, summaries.get(CLAN_1).multiplier(), MSG_MULTIPLIER),
                () -> assertEquals(1.2, summaries.get(CLAN_2).multiplier(), MSG_MULTIPLIER)
        );
    }

    @Test
    void expireStaleModifiers_ShouldCallDeactivateExpired() {
        modifierService.expireStaleModifiers();
        verify(modifierRepository).deactivateExpired(any(Instant.class));
    }
}
