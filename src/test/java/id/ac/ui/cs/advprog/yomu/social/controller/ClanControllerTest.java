package id.ac.ui.cs.advprog.yomu.social.controller;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
import id.ac.ui.cs.advprog.yomu.social.dto.MyClanResponse;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.service.clan.lifecycle.ClanLifecycleService;
import id.ac.ui.cs.advprog.yomu.social.service.clan.membership.ClanMembershipService;
import id.ac.ui.cs.advprog.yomu.social.service.clan.query.ClanQueryService;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClanControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ClanLifecycleService lifecycleService;

    @Mock
    private ClanQueryService queryService;

    @Mock
    private ClanMembershipService membershipService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ClanController clanController;

    private ObjectMapper objectMapper;
    private Clan dummyClan;

    private String clanId;
    private String leaderId;
    private String memberId;
    private String username;
    private String clanName;

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

        deleteSuccessMsg = "Clan berhasil dihapus";
        ClanMember dummyMember = new ClanMember();
        dummyMember.setUsername("LeaderUser");
        dummyMember.setRole("KETUA");
        members = List.of(dummyMember);

        dummyClan = new Clan();
        dummyClan.setId(clanId);
        dummyClan.setName(clanName);
        dummyClan.setLeaderUsername(leaderId);
        dummyClan.setDescription("Clan untuk pecinta buku");
    }

    @Test
    void testCreateClanSuccess() throws Exception {
        ClanRequest request = new ClanRequest();
        request.setName(clanName);

        when(authentication.getName()).thenReturn(username);
        when(lifecycleService.createClan(any(ClanRequest.class))).thenReturn(dummyClan);

        mockMvc.perform(post(BASE_API)
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(clanName))
                .andExpect(jsonPath("$.id").value(clanId));

        verify(lifecycleService, times(1)).createClan(any(ClanRequest.class));
    }

    @Test
    void testGetAllClans() throws Exception {
        id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryResponse summary = new id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryResponse(
                clanId, clanName, "Description", leaderId, "Bronze", 0, 1, 0L, List.of(), List.of());
        when(queryService.findAll(null)).thenReturn(List.of(summary));

        mockMvc.perform(get(BASE_API))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value(clanName));

        verify(queryService, times(1)).findAll(null);
    }

    @Test
    void testGetMyClan_WhenUserHasClan() throws Exception {
        MyClanResponse response = new MyClanResponse(
                dummyClan.getId(),
                dummyClan.getName(),
                dummyClan.getDescription(),
                dummyClan.getLeaderUsername(),
                "KETUA",
                "Bronze",
                100,
                1,
                members);

        when(authentication.getName()).thenReturn(leaderId);
        when(queryService.getMyClanByUsername(leaderId)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/clans/me")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clanId))
                .andExpect(jsonPath("$.role").value("KETUA"))
                .andExpect(jsonPath("$.members[0].username").value("LeaderUser"));

        verify(queryService, times(1)).getMyClanByUsername(leaderId);
    }

    @Test
    void testGetMyClan_WhenUserHasNoClan() throws Exception {
        when(authentication.getName()).thenReturn(memberId);
        when(queryService.getMyClanByUsername(memberId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/clans/me")
                .principal(authentication))
                .andExpect(status().is(200));

        verify(queryService, times(1)).getMyClanByUsername(memberId);
    }

    @Test
    void testEditClan() throws Exception {
        ClanRequest request = new ClanRequest();
        request.setName("New Name");
        request.setDescription("New Description");

        when(authentication.getName()).thenReturn(leaderId);
        when(lifecycleService.editClan(eq(clanId), eq(leaderId), any(ClanRequest.class))).thenReturn(dummyClan);

        mockMvc.perform(post("/api/clans/" + clanId + "/edit")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(lifecycleService, times(1)).editClan(eq(clanId), eq(leaderId), any(ClanRequest.class));
    }

    @Test
    void testDeleteClan() throws Exception {
        when(authentication.getName()).thenReturn(leaderId);

        mockMvc.perform(post("/api/clans/" + clanId + "/delete")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string(deleteSuccessMsg));

        verify(lifecycleService, times(1)).deleteClan(eq(clanId), eq(leaderId));
    }
}
