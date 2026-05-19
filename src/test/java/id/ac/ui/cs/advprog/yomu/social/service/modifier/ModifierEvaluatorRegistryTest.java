package id.ac.ui.cs.advprog.yomu.social.service.modifier;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;
import id.ac.ui.cs.advprog.yomu.social.service.modifier.buff.BuffApplicationService;

@ExtendWith(MockitoExtension.class)
class ModifierEvaluatorRegistryTest {

    private static final String CLAN_ID = "clan-1";

    @Mock
    private BuffApplicationService buffApplicationService;

    private ModifierEvaluatorRegistry registry;

    @Mock
    private ModifierEvaluator evaluator1;

    @Mock
    private ModifierEvaluator evaluator2;

    @BeforeEach
    void setUp() {
        registry = new ModifierEvaluatorRegistry(
                List.of(evaluator1, evaluator2),
                buffApplicationService
        );
    }

    @Test
    void evaluateAll_ShouldApplyOrDeactivateBasedOnEvaluatorResult() {
        ClanQuizStats stats = new ClanQuizStats();

        when(evaluator1.evaluate(CLAN_ID, stats)).thenReturn(Optional.of("BUFF_KEY_1"));

        when(evaluator2.getKey()).thenReturn("BUFF_KEY_2");
        when(evaluator2.evaluate(CLAN_ID, stats)).thenReturn(Optional.empty());

        registry.evaluateAll(CLAN_ID, stats);

        assertAll("Verify modifier registry behaviors",
                () -> verify(buffApplicationService).applyBuff(CLAN_ID, "BUFF_KEY_1"),
                () -> verify(buffApplicationService).deactivateBuff(CLAN_ID, "BUFF_KEY_2")
        );
    }
}
