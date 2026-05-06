package id.ac.ui.cs.advprog.yomu.social.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import id.ac.ui.cs.advprog.yomu.social.dto.ClanLeaderboardRow;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;

@Repository
public interface ClanRepository extends JpaRepository<Clan, String> {

    String TIER_PARAM = "tier";

    Optional<Clan> findByName(String name);
    boolean existsByName(String name);

    @Query("SELECT COUNT(c) FROM Clan c WHERE c.tier = :" + TIER_PARAM)
    long countByTier(@Param(TIER_PARAM) Tier tier);

    @Query("SELECT c FROM Clan c WHERE c.tier = :" + TIER_PARAM + " ORDER BY c.score DESC, c.id ASC")
    List<Clan> findTopClansByTier(@Param(TIER_PARAM) Tier tier, Pageable pageable);

    @Query("SELECT c FROM Clan c WHERE c.tier = :" + TIER_PARAM + " ORDER BY c.score ASC, c.id ASC")
    List<Clan> findBottomClansByTier(@Param(TIER_PARAM) Tier tier, Pageable pageable);

    @Query("""
            SELECT c.id as clanId,
                   c.name as clanName,
                   c.tier as tier,
                   c.score as score,
                   COUNT(m) as memberCount
            FROM Clan c
            LEFT JOIN ClanMember m ON m.clanId = c.id
            WHERE c.tier = :""" + TIER_PARAM + """
            
            GROUP BY c.id, c.name, c.tier, c.score
            ORDER BY c.score DESC, c.id ASC
            """)
    List<ClanLeaderboardRow> findLeaderboardByTier(@Param(TIER_PARAM) Tier tier, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Clan c SET c.score = 0")
    int resetAllScores();
}