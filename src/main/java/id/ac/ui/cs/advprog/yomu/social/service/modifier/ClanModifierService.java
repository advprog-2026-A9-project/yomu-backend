package id.ac.ui.cs.advprog.yomu.social.service.modifier;

import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.social.dto.ModifierSummary;
import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;

public interface ClanModifierService {

    @Transactional
    void evaluateModifiers(String clanId, ClanQuizStats stats);

    @Transactional
    double getActiveMultiplier(String clanId);

    @Transactional(readOnly = true)
    ModifierSummary getModifierSummary(String clanId);
}
