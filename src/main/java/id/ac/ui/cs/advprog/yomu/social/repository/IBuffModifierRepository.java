package id.ac.ui.cs.advprog.yomu.social.repository;

import java.util.Optional;

import id.ac.ui.cs.advprog.yomu.social.model.ClanModifier;

public interface IBuffModifierRepository {
    Optional<ClanModifier> findByClanIdAndKey(String clanId, String key);
    ClanModifier saveModifier(ClanModifier modifier);
}
