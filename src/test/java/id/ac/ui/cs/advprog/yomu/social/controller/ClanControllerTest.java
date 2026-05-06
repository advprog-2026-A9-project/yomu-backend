package id.ac.ui.cs.advprog.yomu.social.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
import id.ac.ui.cs.advprog.yomu.social.dto.MyClanResponse;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.service.ClanService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class ClanControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ClanService clanService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private ClanController clanController;

    private ObjectMapper objectMapper;
    private Clan dummyClan;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private String clanId;
    private String leaderId;
    private String memberId;
    private String username;
    private String clanName;
    private String authHeader;
    private String token;

    private String createSuccessMsg;
    private String deleteSuccessMsg;
    private List<ClanMember> members;
    private final String BASE_API = "/api/clans";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // Setup MockMvc secara standalone
        mockMvc = MockMvcBuilders.standaloneSetup(clanController).build();

        clanId = "clan-123";
        leaderId = "user-456";
        memberId = "user-789";
        username = "wibu";
        clanName = "Wibu Elite";
        token = "dummy-token";
        authHeader = "Bearer " + token;

        createSuccessMsg = "Clan berhasil dibuat";
        deleteSuccessMsg = "Clan berhasil dihapus";
        ClanMember dummyMember = new ClanMember();
        dummyMember.setUserId(leaderId);
        dummyMember.setUsername("LeaderUser");
        dummyMember.setRole("KETUA");
        members = List.of(dummyMember);

        dummyClan = new Clan();
        dummyClan.setId(clanId);
        dummyClan.setName(clanName);
        dummyClan.setLeaderUserId(leaderId);
        dummyClan.setDescription("Clan untuk pecinta buku");
    }

    @Test
    void testCreateClanSuccess() throws Exception {
        ClanRequest request = new ClanRequest();
        request.setName(clanName);

        when(jwtUtil.extractUserId(token)).thenReturn(leaderId);
        when(jwtUtil.extractUsername(token)).thenReturn(username);
        when(clanService.createClan(any(ClanRequest.class))).thenReturn(dummyClan);

        mockMvc.perform(post(BASE_API)
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

        mockMvc.perform(get(BASE_API))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value(clanName));

        verify(clanService, times(1)).findAll();
    }

    @Test
    void testGetMyClan_WhenUserHasClan() throws Exception {
        MyClanResponse response = new MyClanResponse(
                dummyClan.getId(),
                dummyClan.getName(),
                dummyClan.getDescription(),
                dummyClan.getLeaderUserId(),
                "KETUA",
                members);

        when(jwtUtil.extractUserId(token)).thenReturn(leaderId);
        when(clanService.getMyClanByUserId(leaderId)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/clans/me")
                .header(AUTHORIZATION_HEADER, authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clanId))
                .andExpect(jsonPath("$.role").value("KETUA"))
                .andExpect(jsonPath("$.members[0].username").value("LeaderUser"));

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
    void testEditClan() throws Exception {
        ClanRequest request = new ClanRequest();
        request.setName("New Name");
        request.setDescription("New Description");

        when(jwtUtil.extractUserId(token)).thenReturn(leaderId);
        when(clanService.editClan(eq(clanId), eq(leaderId), any(ClanRequest.class))).thenReturn(dummyClan);

        mockMvc.perform(post("/api/clans/" + clanId + "/edit")
                .header(AUTHORIZATION_HEADER, authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(clanService, times(1)).editClan(eq(clanId), eq(leaderId), any(ClanRequest.class));
    }

    @Test
    void testDeleteClan() throws Exception {
        when(jwtUtil.extractUserId(token)).thenReturn(leaderId);

        mockMvc.perform(post("/api/clans/" + clanId + "/delete")
                .header(AUTHORIZATION_HEADER, authHeader))
                .andExpect(status().isOk())
                .andExpect(content().string(deleteSuccessMsg));

        verify(clanService, times(1)).deleteClan(eq(clanId), eq(leaderId));
    }
}
