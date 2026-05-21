package id.ac.ui.cs.advprog.yomu.social.service.modifier;

import java.util.List;
import java.util.Optional;

import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;
import id.ac.ui.cs.advprog.yomu.social.service.modifier.buff.BuffApplicationService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ModifierEvaluatorRegistry implements ModifierEvaluatorRegistryPort {

    private final List<ModifierEvaluator> evaluators;
    private final BuffApplicationService buffApplicationService;

    @Override
    public void evaluateAll(String clanId, ClanQuizStats stats) {
        for (ModifierEvaluator evaluator : evaluators) {
            Optional<String> result = evaluator.evaluate(clanId, stats);
            if (result.isPresent()) {
                buffApplicationService.applyBuff(clanId, result.get());
            } else {
                buffApplicationService.deactivateBuff(clanId, evaluator.getKey());
            }
        }
    }
}