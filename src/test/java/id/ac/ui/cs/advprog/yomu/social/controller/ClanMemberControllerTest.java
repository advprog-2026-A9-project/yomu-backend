package id.ac.ui.cs.advprog.yomu.social.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.service.clan.joinrequest.ClanJoinRequestService;
import id.ac.ui.cs.advprog.yomu.social.service.clan.membership.ClanMembershipService;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class ClanMemberControllerTest {

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
        mockMvc = MockMvcBuilders.standaloneSetup(clanMemberController).build();

        clanId = "clan-123";
        username = "testuser";
    }

    @Test
    void testJoinClan() throws Exception {
        when(authentication.getName()).thenReturn(username);

        mockMvc.perform(post("/api/clans/" + clanId + "/join")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string("Permintaan bergabung berhasil dikirim."));

        verify(joinRequestService, times(1)).requestJoin(eq(clanId), eq(username));
    }

    @Test
    void testLeaveClan() throws Exception {
        when(authentication.getName()).thenReturn(username);

        mockMvc.perform(post("/api/clans/" + clanId + "/leave")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string(SocialConstants.LEAVE_SUCCESS_MESSAGE));

        verify(membershipService, times(1)).leaveClan(eq(clanId), eq(username));
    }
}
