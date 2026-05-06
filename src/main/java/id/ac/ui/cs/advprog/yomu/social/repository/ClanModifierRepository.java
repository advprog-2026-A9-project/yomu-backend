package id.ac.ui.cs.advprog.yomu.social.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import id.ac.ui.cs.advprog.yomu.social.model.ClanModifier;

@Repository
public interface ClanModifierRepository extends JpaRepository<ClanModifier, Long> {
    List<ClanModifier> findByClanIdAndActiveTrue(String clanId);

    Optional<ClanModifier> findByClanIdAndKey(String clanId, String key);
}
