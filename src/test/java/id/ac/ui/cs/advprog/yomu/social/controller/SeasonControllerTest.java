package id.ac.ui.cs.advprog.yomu.social.controller;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.dto.SeasonEndResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.SeasonStatusResponse;
import id.ac.ui.cs.advprog.yomu.social.service.SeasonService;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class SeasonControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SeasonService seasonService;

    @InjectMocks
    private SeasonController seasonController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(seasonController).build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testEndSeasonAsAdmin() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin-user",
                        null,
                        List.of(new SimpleGrantedAuthority("ADMIN"))));

        SeasonEndResponse mockResponse = new SeasonEndResponse(1, 2, List.of(), List.of(), List.of(), List.of());
        when(seasonService.endSeason()).thenReturn(mockResponse);

        mockMvc.perform(post("/api/seasons/end"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newSeasonNumber").value(2));

        verify(seasonService, times(1)).endSeason();
    }

    @Test
    void testEndSeasonAsNonAdmin() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "pelajar-user",
                        null,
                        List.of(new SimpleGrantedAuthority("PELAJAR"))));

        mockMvc.perform(post("/api/seasons/end"))
                .andExpect(status().isForbidden());

        verify(seasonService, never()).endSeason();
    }
}
