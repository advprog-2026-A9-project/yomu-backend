package id.ac.ui.cs.advprog.yomu.social.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import id.ac.ui.cs.advprog.yomu.social.model.ClanModifier;

@Repository
@SuppressWarnings("null")
public interface ClanModifierRepository extends JpaRepository<ClanModifier, Long>, IBuffModifierRepository {
    List<ClanModifier> findByClanIdAndActiveTrue(String clanId);

    @Override
    Optional<ClanModifier> findByClanIdAndKey(String clanId, String key);

    @Override
    default ClanModifier saveModifier(ClanModifier modifier) {
        return this.save(modifier);
    }

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ClanModifier c SET c.active = false WHERE c.active = true")
    void deactivateAllActive(Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ClanModifier c SET c.active = false WHERE c.endAt <= :now")
    void deactivateExpired(@Param("now") Instant now);
}
