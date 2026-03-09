package id.ac.ui.cs.advprog.yomu.social.service;

import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardResponse;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanRepository;
import id.ac.ui.cs.advprog.yomu.social.strategy.ScoringStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for leaderboard functionality - will fail until Tier and scoring are implemented.
 */
@ExtendWith(MockitoExtension.class)
class ClanServiceLeaderboardTest {

    @Mock
    private ClanRepository clanRepository;

    @Mock
    private ClanMemberRepository memberRepository;

    @Mock
    private ScoringStrategyFactory scoringStrategyFactory;

    @InjectMocks
    private ClanServiceImpl clanService;

    private Clan bronzeClan;
    private Clan silverClan;

    @BeforeEach
    void setUp() {
        bronzeClan = new Clan();
        bronzeClan.setId("clan-1");
        bronzeClan.setName("Bronze Warriors");
        bronzeClan.setLeaderUserId("user-1");
        bronzeClan.setTier(Tier.BRONZE);
        bronzeClan.setScore(100);

        silverClan = new Clan();
        silverClan.setId("clan-2");
        silverClan.setName("Silver Champions");
        silverClan.setLeaderUserId("user-2");
        silverClan.setTier(Tier.SILVER);
        silverClan.setScore(200);
    }

    @Test
    void testGetLeaderboardByTier_ReturnsGroupedByTier() {
        // This test will fail until implementation
        when(clanRepository.findAll()).thenReturn(List.of(bronzeClan, silverClan));
        when(memberRepository.countByClanId(anyString())).thenReturn(5L);

        List<LeaderboardResponse> leaderboard = clanService.getLeaderboardByTier();

        assertNotNull(leaderboard);
        assertTrue(leaderboard.size() >= 1);
    }

    @Test
    void testEndSeason_PromotesTopClans() {
        // This test will fail until implementation
        when(clanRepository.findAll()).thenReturn(List.of(bronzeClan));
        when(memberRepository.countByClanId(anyString())).thenReturn(5L);

        assertDoesNotThrow(() -> clanService.endSeason());
        
        verify(clanRepository, atLeastOnce()).save(any(Clan.class));
    }
}
