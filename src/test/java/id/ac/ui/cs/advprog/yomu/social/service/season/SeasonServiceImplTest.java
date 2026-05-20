package id.ac.ui.cs.advprog.yomu.social.service.season;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanModifierRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.SeasonStateRepository;
import id.ac.ui.cs.advprog.yomu.social.mapper.SocialMapper;
import id.ac.ui.cs.advprog.yomu.social.dto.*;
import id.ac.ui.cs.advprog.yomu.social.service.score.ClanQuizStatsService;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class SeasonServiceImplTest {

    @Mock
    private ClanRepository clanRepository;

    @Mock
    private ClanQuizStatsService statsService;

    @Mock
    private ClanMemberRepository memberRepository;

    @Mock
    private SeasonStateRepository seasonStateRepository;

    @Mock
    private ClanModifierRepository modifierRepository;

    @Mock
    private SocialMapper socialMapper;

    @InjectMocks
    private SeasonServiceImpl seasonService;

    private Clan bronzeClan;
    private Clan silverClan;
    private Clan silverTopClan;

    @BeforeEach
    void setUp() {
        bronzeClan = new Clan();
        bronzeClan.setId("clan-1");
        bronzeClan.setTier(Tier.BRONZE);
        bronzeClan.setScore(100);

        silverClan = new Clan();
        silverClan.setId("clan-2");
        silverClan.setTier(Tier.SILVER);
        silverClan.setScore(200);

        silverTopClan = new Clan();
        silverTopClan.setId("clan-3");
        silverTopClan.setTier(Tier.SILVER);
        silverTopClan.setScore(250);

        // Default stubbing to avoid PotentialStubbingProblem when endSeason loops all
        // tiers
        for (Tier t : Tier.values()) {
            when(clanRepository.countByTier(t)).thenReturn(0L);
        }

        when(seasonStateRepository.findTopByOrderByIdDesc()).thenReturn(Optional.empty());
        when(socialMapper.toDefaultSeasonStatusResponse()).thenReturn(new SeasonStatusResponse(1, "Active"));
        lenient().when(socialMapper.toSeasonClanSummary(any(), any(Integer.class)))
                .thenReturn(new SeasonClanSummary("id", "name", "tier", 0, 0));
        lenient().when(
                socialMapper.toSeasonEndResponse(any(Integer.class), any(Integer.class), any(), any(), any(), any()))
                .thenReturn(new SeasonEndResponse(1, 2, List.of(), List.of(), List.of(), List.of()));
    }

    @Test
    void testEndSeason_ShouldPromoteTopClans() {
        when(clanRepository.countByTier(Tier.BRONZE)).thenReturn(10L);
        when(clanRepository.findTopClansByTier(eq(Tier.BRONZE), any())).thenReturn(List.of(bronzeClan));

        seasonService.endSeason();

        assertAll("Verify bronze clan promotion",
                () -> assertEquals(Tier.SILVER, bronzeClan.getTier(), "Bronze clan should be promoted to Silver"),
                () -> assertEquals(0, bronzeClan.getScore(), "Promoted clan score should be reset"),
                () -> verify(clanRepository, atLeastOnce()).saveAllAndFlush(any()));
    }

    @Test
    void testEndSeason_ShouldNotReevaluatePromotedClanInLaterTier() {
        when(clanRepository.countByTier(Tier.BRONZE)).thenReturn(10L);
        when(clanRepository.countByTier(Tier.SILVER)).thenReturn(10L);

        when(clanRepository.findTopClansByTier(eq(Tier.BRONZE), any())).thenReturn(List.of(bronzeClan));
        when(clanRepository.findTopClansByTier(eq(Tier.SILVER), any())).thenReturn(List.of(silverClan));
        lenient().when(clanRepository.findBottomClansByTier(eq(Tier.SILVER), any())).thenAnswer(invocation -> {
            if (bronzeClan.getTier() == Tier.SILVER) {
                return List.of(bronzeClan);
            }
            return List.of();
        });

        seasonService.endSeason();

        assertAll("Verify promoted bronze clan is not demoted again in the same run",
                () -> assertEquals(Tier.SILVER, bronzeClan.getTier(), "Bronze clan should stay promoted to Silver"),
                () -> assertEquals(Tier.GOLD, silverClan.getTier(), "Silver clan should still be promoted to Gold"));
    }

    @Test
    void testEndSeason_ShouldDemoteBottomClans() {
        when(clanRepository.countByTier(Tier.SILVER)).thenReturn(10L);
        when(clanRepository.findTopClansByTier(eq(Tier.SILVER), any())).thenReturn(List.of(silverTopClan, silverClan));

        seasonService.endSeason();

        assertAll("Verify silver clan demotion",
                () -> assertEquals(Tier.BRONZE, silverClan.getTier(), "Silver clan should be demoted to Bronze"),
                () -> assertEquals(0, silverClan.getScore(), "Demoted clan score should be reset"),
                () -> verify(clanRepository, atLeastOnce()).saveAllAndFlush(any()));
    }

    @Test
    void testEndSeason_ShouldTriggerGlobalResets() {
        seasonService.endSeason();

        assertAll("Verify global resets",
                () -> verify(clanRepository).resetAllScores(),
                () -> verify(statsService).resetSeasonStats(),
                () -> verify(modifierRepository).deactivateAllActive(any(java.time.Instant.class)));
    }
}
