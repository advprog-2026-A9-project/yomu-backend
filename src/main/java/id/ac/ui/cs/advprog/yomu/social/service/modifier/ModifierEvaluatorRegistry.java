package id.ac.ui.cs.advprog.yomu.social.service.modifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ModifierEvaluatorRegistry {

    private final List<ModifierEvaluator> evaluators;

    private Map<String, ModifierEvaluator> getEvaluatorMap() {
        Map<String, ModifierEvaluator> map = new HashMap<>();
        for (ModifierEvaluator evaluator : evaluators) {
            map.put(evaluator.getKey(), evaluator);
        }
        return map;
    }

    public Optional<ModifierEvaluator> getEvaluator(String key) {
        return Optional.ofNullable(getEvaluatorMap().get(key));
    }

    public void evaluate(String key, String clanId, id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats stats) {
        getEvaluator(key).ifPresent(evaluator -> evaluator.evaluate(clanId, stats));
    }

    public void evaluateAll(String clanId, id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats stats) {
        for (ModifierEvaluator evaluator : evaluators) {
            evaluator.evaluate(clanId, stats);
        }
    }
}