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
import id.ac.ui.cs.advprog.yomu.social.port.ClanLookupPort;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@Repository
public interface ClanRepository extends JpaRepository<Clan, String>, ClanLookupPort {

        @Override
        Optional<Clan> findClanById(String id);

        Optional<Clan> findByName(String name);

        boolean existsByName(String name);

        @Query("SELECT COUNT(c) FROM Clan c WHERE c.tier = :tier")
        long countByTier(@Param("tier") Tier tier);

        @Query("SELECT c FROM Clan c WHERE c.tier = :tier ORDER BY c.score DESC, c.id ASC")
        List<Clan> findTopClansByTier(@Param("tier") Tier tier, Pageable pageable);

        @Query("SELECT c FROM Clan c WHERE c.tier = :tier ORDER BY c.score ASC, c.id ASC")
        List<Clan> findBottomClansByTier(@Param("tier") Tier tier, Pageable pageable);

        @Query("""
                        SELECT c.id as clanId,
                               c.name as clanName,
                               c.tier as tier,
                               c.score as score,
                               COUNT(m) as memberCount
                        FROM Clan c
                        LEFT JOIN ClanMember m ON m.clanId = c.id
                        WHERE c.tier = :tier
                        GROUP BY c.id, c.name, c.tier, c.score
                        ORDER BY c.score DESC, c.id ASC
                        """)
        List<ClanLeaderboardRow> findLeaderboardByTier(@Param("tier") Tier tier, Pageable pageable);

        @Query("""
                        SELECT c.id as clanId,
                               c.name as clanName,
                               c.description as description,
                               c.leaderUsername as leaderUsername,
                               c.tier as tier,
                               c.score as score,
                               COUNT(m) as memberCount
                        FROM Clan c
                        LEFT JOIN ClanMember m ON m.clanId = c.id
                        GROUP BY c.id, c.name, c.description, c.leaderUsername, c.tier, c.score
                        ORDER BY c.score DESC, c.id ASC
                        """)
        List<id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryRow> findAllClanSummaries();

        @Query(value = """
                        SELECT c.id as clanId,
                               c.name as clanName,
                               c.description as description,
                               c.leader_username as leaderUsername,
                               c.tier as tier,
                               c.score as score,
                               COUNT(m.username) as memberCount
                        FROM clans c
                        LEFT JOIN clan_members m ON m.clan_id = c.id
                        GROUP BY c.id, c.name, c.description, c.leader_username, c.tier, c.score
                        ORDER BY RAND()
                        LIMIT :limit
                        """, nativeQuery = true)
        List<id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryRow> findRandomClanSummaries(@Param("limit") int limit);

        @Query("""
                        SELECT c.id as clanId,
                               c.name as clanName,
                               c.description as description,
                               c.leaderUsername as leaderUsername,
                               c.tier as tier,
                               c.score as score,
                               COUNT(m) as memberCount
                        FROM Clan c
                        LEFT JOIN ClanMember m ON m.clanId = c.id
                        WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))
                           OR LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%'))
                        GROUP BY c.id, c.name, c.description, c.leaderUsername, c.tier, c.score
                        ORDER BY c.score DESC, c.id ASC
                        """)
        List<id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryRow> findClanSummariesByQuery(@Param("query") String query);

        @Query("""
                        SELECT c.id as clanId,
                               c.name as clanName,
                               c.tier as tier,
                               c.score as score,
                               COUNT(m) as memberCount
                        FROM Clan c
                        LEFT JOIN ClanMember m ON m.clanId = c.id
                        WHERE c.tier = :tier AND LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))
                        GROUP BY c.id, c.name, c.tier, c.score
                        ORDER BY c.score DESC, c.id ASC
                        """)
        List<ClanLeaderboardRow> findLeaderboardByTierAndName(@Param("tier") Tier tier, @Param("query") String query,
                        Pageable pageable);

        @Query("SELECT COUNT(c) + 1 FROM Clan c WHERE c.tier = :tier AND (c.score > :score OR (c.score = :score AND c.id < :id))")
        long findRankByTierAndScore(@Param("tier") Tier tier, @Param("score") int score, @Param("id") String id);

        @Query("SELECT COUNT(c) FROM Clan c WHERE c.tier = :tier AND c.leaderUsername = :leaderUsername")
        long countByTierAndLeaderUsername(@Param("tier") Tier tier, @Param("leaderUsername") String leaderUsername);

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("UPDATE Clan c SET c.score = 0")
        int resetAllScores();
}