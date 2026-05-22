package id.ac.ui.cs.advprog.yomu.social.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardEntryResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardResponse;
import id.ac.ui.cs.advprog.yomu.social.service.clan.query.ClanQueryService;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class ClanLeaderboardControllerTest {

    private static final String BRONZE = "Bronze";

    private MockMvc mockMvc;

    @Mock
    private ClanQueryService queryService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ClanLeaderboardController clanLeaderboardController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clanLeaderboardController).build();
    }

    @Test
    void testGetLeaderboard_WithAuth() throws Exception {
        String clanId = "clan-123";
        String clanName = "Wibu Elite";
        String username = "user-123";
        LeaderboardEntryResponse entry = new LeaderboardEntryResponse(clanId, clanName, BRONZE, 100, 1, 10);
        LeaderboardResponse leaderboard = new LeaderboardResponse(BRONZE, List.of(entry), entry);

        when(authentication.getName()).thenReturn(username);
        when(queryService.getLeaderboardByTier(eq(username), any())).thenReturn(List.of(leaderboard));

        mockMvc.perform(get("/api/clans/leaderboard")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tier").value(BRONZE));

        verify(queryService, times(1)).getLeaderboardByTier(eq(username), any());
    }

    @Test
    void testGetLeaderboard_WithoutAuth() throws Exception {
        LeaderboardEntryResponse entry = new LeaderboardEntryResponse("clan-1", "Name", BRONZE, 100, 1, 10);
        LeaderboardResponse leaderboard = new LeaderboardResponse(BRONZE, List.of(entry), entry);

        when(queryService.getLeaderboardByTier(eq(null), any())).thenReturn(List.of(leaderboard));

        mockMvc.perform(get("/api/clans/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tier").value(BRONZE));

        verify(queryService, times(1)).getLeaderboardByTier(eq(null), any());
    }
}
