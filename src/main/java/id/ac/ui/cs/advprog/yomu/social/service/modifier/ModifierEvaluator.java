package id.ac.ui.cs.advprog.yomu.social.service.modifier;

import id.ac.ui.cs.advprog.yomu.social.model.ClanModifier;
import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;

public interface ModifierEvaluator {
    String getKey();
    void evaluate(String clanId, ClanQuizStats stats);
    ClanModifier createModifier(String clanId, boolean apply);
}