package id.ac.ui.cs.advprog.yomu.social.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import id.ac.ui.cs.advprog.yomu.social.dto.MyClanResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.service.ClanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClanController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClanService clanService;

    @MockBean
    private JwtUtil jwtUtil;

    private ObjectMapper objectMapper;
    private Clan dummyClan;

    // String constants/variables
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private String clanId;
    private String leaderId;
    private String memberId;
    private String username;
    private String clanName;
    private String authHeader;
    private String token;
    private String joinSuccessMsg;
    private String leaveSuccessMsg;
    private String deleteSuccessMsg;
    private List<ClanMember> members;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        
        // Initialize variables
        clanId = "clan-123";
        leaderId = "user-456";
        memberId = "user-789";
        username = "wibu";
        clanName = "Wibu Elite";
        token = "dummy-token";
        authHeader = "Bearer " + token;
        joinSuccessMsg = "Berhasil bergabung";
        leaveSuccessMsg = "Berhasil keluar dari clan";
        deleteSuccessMsg = "Clan berhasil dihapus";
        ClanMember dummyMember = new ClanMember();
        dummyMember.setUserId(leaderId);
        dummyMember.setUsername("LeaderUser");
        List<ClanMember> members = List.of(dummyMember);

        dummyClan = new Clan();
        dummyClan.setId(clanId);
        dummyClan.setName(clanName);
        dummyClan.setLeaderUserId(leaderId);
    }

    @Test
    void testCreateClanSuccess() throws Exception {
        ClanRequest request = new ClanRequest();
        request.setName(clanName);
        request.setUserId(leaderId);

        when(clanService.createClan(any(ClanRequest.class))).thenReturn(dummyClan);
        when(jwtUtil.extractUserId(token)).thenReturn(leaderId);

        mockMvc.perform(post("/api/clans")
            .header(AUTHORIZATION_HEADER, authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(clanName))
                .andExpect(jsonPath("$.id").value(clanId));

        verify(clanService, times(1)).createClan(any(ClanRequest.class));
    }

    @Test
    void testGetAllClans() throws Exception {
        List<Clan> clans = Arrays.asList(dummyClan);
        when(clanService.findAll()).thenReturn(clans);

        mockMvc.perform(get("/api/clans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value(clanName));

        verify(clanService, times(1)).findAll();
    }

    @Test
    void testJoinClan() throws Exception {
        when(jwtUtil.extractUserId(token)).thenReturn(memberId);

        mockMvc.perform(post("/api/clans/" + clanId + "/join")
            .header(AUTHORIZATION_HEADER, authHeader)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(joinSuccessMsg));

        verify(clanService, times(1)).joinClan(eq(clanId), eq(memberId), eq(username), "MEMBER");
    }

    @Test
    void testLeaveClan() throws Exception {
        when(jwtUtil.extractUserId(token)).thenReturn(leaderId);

        mockMvc.perform(post("/api/clans/" + clanId + "/leave")
            .header(AUTHORIZATION_HEADER, authHeader)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(leaveSuccessMsg));

        verify(clanService, times(1)).leaveClan(eq(clanId), eq(leaderId));
    }

    @Test
    void testDeleteClan() throws Exception {
        when(jwtUtil.extractUserId(token)).thenReturn(leaderId);

        mockMvc.perform(post("/api/clans/" + clanId + "/delete")
            .header(AUTHORIZATION_HEADER, authHeader)
            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(deleteSuccessMsg));

        verify(clanService, times(1)).deleteClan(eq(clanId), eq(leaderId));
    }

        @Test
        void testGetMyClan_WhenUserHasClan() throws Exception {
        MyClanResponse response = new MyClanResponse(dummyClan.getId(), dummyClan.getName(),
            dummyClan.getDescription(), dummyClan.getLeaderUserId(), "KETUA", members );
        when(jwtUtil.extractUserId(token)).thenReturn(leaderId);
        when(clanService.getMyClanByUserId(leaderId)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/clans/me")
            .header("Authorization", authHeader))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(clanId))
            .andExpect(jsonPath("$.role").value("KETUA"));

        verify(clanService, times(1)).getMyClanByUserId(leaderId);
        }

        @Test
        void testGetMyClan_WhenUserHasNoClan() throws Exception {
        when(jwtUtil.extractUserId(token)).thenReturn(memberId);
        when(clanService.getMyClanByUserId(memberId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/clans/me")
            .header(AUTHORIZATION_HEADER, authHeader))
            .andExpect(status().isNotFound());

        verify(clanService, times(1)).getMyClanByUserId(memberId);
        }

    @Test
    void testGetLeaderboard() throws Exception {
        // This test will fail until we implement the endpoint
        mockMvc.perform(get("/api/clans/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(clanService, times(1)).getLeaderboardByTier();
    }

    @Test
    void testEndSeason_AsAdmin() throws Exception {
        // This test will fail until we implement the endpoint
        when(jwtUtil.extractUserId(token)).thenReturn("admin-user");

        mockMvc.perform(post("/api/clans/admin/end-season")
            .header(AUTHORIZATION_HEADER, authHeader))
                .andExpect(status().isOk())
                .andExpect(content().string("Season ended. Clans promoted/demoted."));

        verify(clanService, times(1)).endSeason();
    }
}