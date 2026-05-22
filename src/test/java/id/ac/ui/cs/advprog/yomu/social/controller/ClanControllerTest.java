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
<<<<<<< HEAD
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
=======
>>>>>>> origin/staging
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
<<<<<<< HEAD
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanDetailResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryResponse;
=======

import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
>>>>>>> origin/staging
import id.ac.ui.cs.advprog.yomu.social.dto.MyClanResponse;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.model.ClanRole;
import id.ac.ui.cs.advprog.yomu.social.service.clan.lifecycle.ClanLifecycleService;
import id.ac.ui.cs.advprog.yomu.social.service.clan.membership.ClanMembershipService;
import id.ac.ui.cs.advprog.yomu.social.service.clan.query.ClanQueryService;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClanControllerTest {

<<<<<<< HEAD
    private static final String BRONZE = "Bronze";
    private static final String CLANS_PATH = "/api/clans/";

=======
>>>>>>> origin/staging
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
<<<<<<< HEAD
        objectMapper.registerModule(new JavaTimeModule());

        // Setup MockMvc secara standalone dengan Jackson Message Converter dan String Message Converter
        mockMvc = MockMvcBuilders.standaloneSetup(clanController)
                .setMessageConverters(
                        new org.springframework.http.converter.StringHttpMessageConverter(),
                        new MappingJackson2HttpMessageConverter(objectMapper)
                )
                .build();
=======

        // Setup MockMvc secara standalone
        mockMvc = MockMvcBuilders.standaloneSetup(clanController).build();
>>>>>>> origin/staging

        clanId = "clan-123";
        leaderId = "user-456";
        memberId = "user-789";
        username = "wibu";
        clanName = "Wibu Elite";

        deleteSuccessMsg = "Clan berhasil dihapus";
        ClanMember dummyMember = new ClanMember();
        dummyMember.setUsername("LeaderUser");
        dummyMember.setRole(ClanRole.LEADER);
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
<<<<<<< HEAD
        ClanSummaryResponse summary = new ClanSummaryResponse(
                clanId, clanName, "Description", leaderId, BRONZE, 0, 1, 0L, List.of(), List.of());
=======
        id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryResponse summary = new id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryResponse(
                clanId, clanName, "Description", leaderId, "Bronze", 0, 1, 0L, List.of(), List.of());
>>>>>>> origin/staging
        when(queryService.findAll(null)).thenReturn(List.of(summary));

        mockMvc.perform(get(BASE_API))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value(clanName));

        verify(queryService, times(1)).findAll(null);
    }

    @Test
<<<<<<< HEAD
    void testGetAllClans_Random() throws Exception {
        ClanSummaryResponse summary = new ClanSummaryResponse(
                clanId, clanName, "Description", leaderId, BRONZE, 0, 1, 0L, List.of(), List.of());
        when(queryService.findRandomClans(10)).thenReturn(List.of(summary));

        mockMvc.perform(get(BASE_API)
                .param("random", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value(clanName));

        verify(queryService, times(1)).findRandomClans(10);
    }

    @Test
=======
>>>>>>> origin/staging
    void testGetMyClan_WhenUserHasClan() throws Exception {
        MyClanResponse response = new MyClanResponse(
                dummyClan.getId(),
                dummyClan.getName(),
                dummyClan.getDescription(),
                dummyClan.getLeaderUsername(),
                "KETUA",
<<<<<<< HEAD
                BRONZE,
=======
                "Bronze",
>>>>>>> origin/staging
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
<<<<<<< HEAD
    void testGetClanDetail() throws Exception {
        ClanDetailResponse detail = new ClanDetailResponse(
                clanId, clanName, "Desc", leaderId, BRONZE, 1, 100, 1, 10, 85.0, List.of(), List.of(), List.of());

        when(queryService.getClanDetail(clanId)).thenReturn(detail);

        mockMvc.perform(get(CLANS_PATH + clanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clanId))
                .andExpect(jsonPath("$.name").value(clanName));

        verify(queryService, times(1)).getClanDetail(clanId);
    }

    @Test
=======
>>>>>>> origin/staging
    void testEditClan() throws Exception {
        ClanRequest request = new ClanRequest();
        request.setName("New Name");
        request.setDescription("New Description");

        when(authentication.getName()).thenReturn(leaderId);
        when(lifecycleService.editClan(eq(clanId), eq(leaderId), any(ClanRequest.class))).thenReturn(dummyClan);

<<<<<<< HEAD
        mockMvc.perform(post(CLANS_PATH + clanId + "/edit")
=======
        mockMvc.perform(post("/api/clans/" + clanId + "/edit")
>>>>>>> origin/staging
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(lifecycleService, times(1)).editClan(eq(clanId), eq(leaderId), any(ClanRequest.class));
    }

    @Test
    void testDeleteClan() throws Exception {
        when(authentication.getName()).thenReturn(leaderId);

<<<<<<< HEAD
        mockMvc.perform(post(CLANS_PATH + clanId + "/delete")
=======
        mockMvc.perform(post("/api/clans/" + clanId + "/delete")
>>>>>>> origin/staging
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string(deleteSuccessMsg));

        verify(lifecycleService, times(1)).deleteClan(eq(clanId), eq(leaderId));
    }
<<<<<<< HEAD

    @Test
    void testKickMember() throws Exception {
        when(authentication.getName()).thenReturn(leaderId);

        mockMvc.perform(post(CLANS_PATH + clanId + "/kick/" + memberId)
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string(SocialConstants.KICK_SUCCESS_MESSAGE));

        verify(membershipService, times(1)).kickMember(eq(clanId), eq(leaderId), eq(memberId));
    }
=======
>>>>>>> origin/staging
}
