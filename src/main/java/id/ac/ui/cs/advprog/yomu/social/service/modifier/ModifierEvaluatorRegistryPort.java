package id.ac.ui.cs.advprog.yomu.social.service.modifier;

import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;

public interface ModifierEvaluatorRegistryPort {
    void evaluateAll(String clanId, ClanQuizStats stats);
}