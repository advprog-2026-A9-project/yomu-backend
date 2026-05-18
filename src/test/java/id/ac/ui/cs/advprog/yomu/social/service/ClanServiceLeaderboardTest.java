package id.ac.ui.cs.advprog.yomu.social.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardResponse;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanRepository;
import id.ac.ui.cs.advprog.yomu.social.strategy.ScoringStrategyResolver;
import id.ac.ui.cs.advprog.yomu.social.validation.ClanValidator;
import id.ac.ui.cs.advprog.yomu.social.mapper.SocialMapper;
import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardEntryResponse;
import static org.mockito.Mockito.lenient;
import static org.mockito.ArgumentMatchers.anyInt;

/**
 * Tests for leaderboard functionality - will fail until Tier and scoring are
 * implemented.
 */
@ExtendWith(MockitoExtension.class)
class ClanServiceLeaderboardTest {

    @Mock
    private ClanRepository clanRepository;

    @Mock
    private ClanMemberRepository memberRepository;

    @Mock
    private ScoringStrategyResolver scoringStrategyFactory;

    @Mock
    private ClanModifierService modifierService;

    @Mock
    private ClanQuizStatsService statsService;

    @Mock
    private ClanValidator clanValidation;

    @Mock
    private SocialMapper socialMapper;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

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

        lenient().when(socialMapper.toLeaderboardEntryResponse(any(), anyInt())).thenReturn(new LeaderboardEntryResponse("id", "name", "tier", 0, 1, 1));
        lenient().when(socialMapper.toLeaderboardEntryResponse(any(Clan.class), anyInt(), anyInt())).thenReturn(new LeaderboardEntryResponse("id", "name", "tier", 0, 1, 1));
    }

    @Test
    void testGetLeaderboardByTier_ShouldNotReturnNull() {
        when(clanRepository.findLeaderboardByTier(any(Tier.class), any())).thenReturn(List.of());

        List<LeaderboardResponse> leaderboard = clanService.getLeaderboardByTier("user-1", null);

        assertNotNull(leaderboard, "Leaderboard should not be null");
    }

    @Test
    void testGetLeaderboardByTier_ShouldContainTiers() {
        when(clanRepository.findLeaderboardByTier(any(Tier.class), any())).thenReturn(List.of());

        List<LeaderboardResponse> leaderboard = clanService.getLeaderboardByTier("user-1", null);

        assertFalse(leaderboard.isEmpty(), "Leaderboard should contain at least one tier");
    }

}
