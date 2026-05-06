package id.ac.ui.cs.advprog.yomu.social.service;

import java.util.List;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanRepository;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class SeasonServiceImplTest {

    @Mock
    private ClanRepository clanRepository;

    @Mock
    private ClanQuizStatsService statsService;

    @Mock
    private ClanModifierService modifierService;

    @InjectMocks
    private SeasonServiceImpl seasonService;

    private Clan bronzeClan;
    private Clan silverClan;

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

        // Default stubbing to avoid PotentialStubbingProblem when endSeason loops all
        // tiers
        for (Tier t : Tier.values()) {
            when(clanRepository.countByTier(t)).thenReturn(0L);
        }
    }

    @Test
    void testEndSeason_ShouldPromoteTopClans() {
        when(clanRepository.countByTier(Tier.BRONZE)).thenReturn(10L);
        when(clanRepository.findTopClansByTier(eq(Tier.BRONZE), any())).thenReturn(List.of(bronzeClan));
        when(clanRepository.findBottomClansByTier(eq(Tier.BRONZE), any())).thenReturn(List.of());

        seasonService.endSeason();

        assertAll("Verify bronze clan promotion",
                () -> assertEquals(Tier.SILVER, bronzeClan.getTier(), "Bronze clan should be promoted to Silver"),
                () -> assertEquals(0, bronzeClan.getScore(), "Promoted clan score should be reset"),
                () -> verify(clanRepository, atLeastOnce()).saveAllAndFlush(any()));
    }

    @Test
    void testEndSeason_ShouldDemoteBottomClans() {
        when(clanRepository.countByTier(Tier.SILVER)).thenReturn(10L);
        when(clanRepository.findTopClansByTier(eq(Tier.SILVER), any())).thenReturn(List.of());
        when(clanRepository.findBottomClansByTier(eq(Tier.SILVER), any())).thenReturn(List.of(silverClan));

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
                () -> verify(modifierService).clearSeasonModifiers());
    }
}
