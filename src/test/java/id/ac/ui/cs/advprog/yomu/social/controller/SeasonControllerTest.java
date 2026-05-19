package id.ac.ui.cs.advprog.yomu.social.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import id.ac.ui.cs.advprog.yomu.social.dto.SeasonEndResponse;
import id.ac.ui.cs.advprog.yomu.social.service.season.SeasonService;

@WebMvcTest(SeasonController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("null")
class SeasonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SeasonService seasonService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser(authorities = { "ROLE_ADMIN" })
    void testEndSeasonAsAdmin() throws Exception {
        SeasonEndResponse mockResponse = new SeasonEndResponse(1, 2, List.of(), List.of(), List.of(), List.of());
        when(seasonService.endSeason()).thenReturn(mockResponse);

        mockMvc.perform(post("/api/seasons/end"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newSeasonNumber").value(2));

        verify(seasonService, times(1)).endSeason();
    }

}
