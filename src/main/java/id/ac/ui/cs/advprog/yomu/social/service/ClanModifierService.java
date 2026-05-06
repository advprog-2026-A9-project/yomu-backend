package id.ac.ui.cs.advprog.yomu.social.service;

import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;

public interface ClanModifierService {

    @Transactional
    public void evaluateModifiers(String clanId, ClanQuizStats stats);

    @Transactional
    public double getActiveMultiplier(String clanId);

    @Transactional
    public void clearSeasonModifiers();
}
