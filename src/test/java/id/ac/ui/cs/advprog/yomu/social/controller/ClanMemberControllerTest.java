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
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.service.ClanService;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class ClanMemberControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ClanService clanService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private ClanMemberController clanMemberController;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private String clanId;
    private String userId;
    private String username;
    private String authHeader;
    private String token;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clanMemberController).build();

        clanId = "clan-123";
        userId = "user-456";
        username = "testuser";
        token = "dummy-token";
        authHeader = "Bearer " + token;
    }

    @Test
    void testJoinClan() throws Exception {
        when(jwtUtil.extractUserId(token)).thenReturn(userId);
        when(jwtUtil.extractUsername(token)).thenReturn(username);

        mockMvc.perform(post("/api/clans/" + clanId + "/join")
                .header(AUTHORIZATION_HEADER, authHeader))
                .andExpect(status().isOk())
                .andExpect(content().string("Permintaan bergabung berhasil dikirim."));

        verify(clanService, times(1)).requestJoin(eq(clanId), eq(userId), eq(username));
    }

    @Test
    void testLeaveClan() throws Exception {
        when(jwtUtil.extractUserId(token)).thenReturn(userId);

        mockMvc.perform(post("/api/clans/" + clanId + "/leave")
                .header(AUTHORIZATION_HEADER, authHeader))
                .andExpect(status().isOk())
                .andExpect(content().string(SocialConstants.LEAVE_SUCCESS_MESSAGE));

        verify(clanService, times(1)).leaveClan(eq(clanId), eq(userId));
    }
}
