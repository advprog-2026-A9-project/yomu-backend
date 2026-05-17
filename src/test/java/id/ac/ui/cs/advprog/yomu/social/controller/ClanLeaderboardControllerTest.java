package id.ac.ui.cs.advprog.yomu.social.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardEntryResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardResponse;
import id.ac.ui.cs.advprog.yomu.social.service.ClanService;
import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class ClanLeaderboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ClanService clanService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private ClanLeaderboardController clanLeaderboardController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clanLeaderboardController).build();
    }

    @Test
    void testGetLeaderboard() throws Exception {
        String clanId = "clan-123";
        String clanName = "Wibu Elite";
        String userId = "user-123";
        LeaderboardEntryResponse entry = new LeaderboardEntryResponse(clanId, clanName, "Bronze", 100, 1, 10);
        LeaderboardResponse leaderboard = new LeaderboardResponse("Bronze", List.of(entry), entry);

        when(jwtUtil.extractUserId(anyString())).thenReturn(userId);
        when(clanService.getLeaderboardByTier(anyString(), any())).thenReturn(List.of(leaderboard));

        mockMvc.perform(get("/api/clans/leaderboard")
                .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tier").value("Bronze"));

        verify(clanService, times(1)).getLeaderboardByTier(anyString(), any());
    }
}
