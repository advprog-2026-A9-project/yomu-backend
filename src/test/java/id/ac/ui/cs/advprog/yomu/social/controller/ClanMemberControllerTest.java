package id.ac.ui.cs.advprog.yomu.social.controller;

<<<<<<< HEAD
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyInt;
=======
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
>>>>>>> origin/staging
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
<<<<<<< HEAD
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanJoinRequestResponse;
=======
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
>>>>>>> origin/staging
import id.ac.ui.cs.advprog.yomu.social.service.clan.joinrequest.ClanJoinRequestService;
import id.ac.ui.cs.advprog.yomu.social.service.clan.membership.ClanMembershipService;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class ClanMemberControllerTest {

<<<<<<< HEAD
    private static final String CLANS_PATH = "/api/clans/";
    private static final String LEADER = "leader";

=======
>>>>>>> origin/staging
    private MockMvc mockMvc;

    @Mock
    private ClanJoinRequestService joinRequestService;

    @Mock
    private ClanMembershipService membershipService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ClanMemberController clanMemberController;

    private String clanId;
    private String username;

    @BeforeEach
    void setUp() {
<<<<<<< HEAD
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(clanMemberController)
                .setMessageConverters(
                        new org.springframework.http.converter.StringHttpMessageConverter(),
                        new MappingJackson2HttpMessageConverter(mapper)
                )
                .build();
=======
        mockMvc = MockMvcBuilders.standaloneSetup(clanMemberController).build();
>>>>>>> origin/staging

        clanId = "clan-123";
        username = "testuser";
    }

    @Test
    void testJoinClan() throws Exception {
        when(authentication.getName()).thenReturn(username);

<<<<<<< HEAD
        mockMvc.perform(post(CLANS_PATH + clanId + "/join")
=======
        mockMvc.perform(post("/api/clans/" + clanId + "/join")
>>>>>>> origin/staging
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string("Permintaan bergabung berhasil dikirim."));

        verify(joinRequestService, times(1)).requestJoin(eq(clanId), eq(username));
    }

    @Test
<<<<<<< HEAD
    void testGetRequests() throws Exception {
        when(authentication.getName()).thenReturn(LEADER);
        ClanJoinRequestResponse reqResponse = new ClanJoinRequestResponse(
                1L, clanId, username, "PENDING", LocalDateTime.now());
        Page<ClanJoinRequestResponse> page = new PageImpl<>(List.of(reqResponse), PageRequest.of(0, 10), 1);

        when(joinRequestService.getJoinRequests(eq(clanId), eq(LEADER), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get(CLANS_PATH + clanId + "/requests")
                .principal(authentication)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value(username));

        verify(joinRequestService, times(1)).getJoinRequests(eq(clanId), eq(LEADER), eq(0), eq(10));
    }

    @Test
    void testAcceptRequest() throws Exception {
        when(authentication.getName()).thenReturn(LEADER);

        mockMvc.perform(post(CLANS_PATH + clanId + "/requests/1/accept")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string("Request diterima."));

        verify(joinRequestService, times(1)).acceptJoinRequest(eq(clanId), eq(1L), eq(LEADER));
    }

    @Test
    void testRejectRequest() throws Exception {
        when(authentication.getName()).thenReturn(LEADER);

        mockMvc.perform(post(CLANS_PATH + clanId + "/requests/1/reject")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string("Request ditolak."));

        verify(joinRequestService, times(1)).rejectJoinRequest(eq(clanId), eq(1L), eq(LEADER));
    }

    @Test
    void testRejectAllRequests() throws Exception {
        when(authentication.getName()).thenReturn(LEADER);

        mockMvc.perform(post(CLANS_PATH + clanId + "/requests/reject-all")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string("Semua request berhasil ditolak."));

        verify(joinRequestService, times(1)).rejectAllJoinRequests(eq(clanId), eq(LEADER));
    }

    @Test
    void testLeaveClan() throws Exception {
        when(authentication.getName()).thenReturn(username);

        mockMvc.perform(post(CLANS_PATH + clanId + "/leave")
=======
    void testLeaveClan() throws Exception {
        when(authentication.getName()).thenReturn(username);

        mockMvc.perform(post("/api/clans/" + clanId + "/leave")
>>>>>>> origin/staging
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string(SocialConstants.LEAVE_SUCCESS_MESSAGE));

        verify(membershipService, times(1)).leaveClan(eq(clanId), eq(username));
    }
}
