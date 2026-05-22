package id.ac.ui.cs.advprog.yomu.gamification.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import id.ac.ui.cs.advprog.yomu.gamification.dto.ShowcaseUpdateRequest;
import id.ac.ui.cs.advprog.yomu.gamification.service.achievement.AchievementShowcaseService;

@WebMvcTest(AchievementShowcaseController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("null")
class AchievementShowcaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AchievementShowcaseService showcaseService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void getShowcase_ShouldReturnList() throws Exception {
        when(showcaseService.getShowcaseByUsername("user-1")).thenReturn(List.of("ach-1", "ach-2"));

        mockMvc.perform(get("/api/gamification/showcase").param("username", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("ach-1"))
                .andExpect(jsonPath("$[1]").value("ach-2"));

        verify(showcaseService).getShowcaseByUsername("user-1");
    }

    @Test
    void updateShowcase_ShouldCallService() throws Exception {
        doNothing().when(showcaseService).updateShowcase(any(ShowcaseUpdateRequest.class));

        String requestJson = """
                {
                    "username": "user-1",
                    "achievementIds": ["ach-1"]
                }
                """;

        mockMvc.perform(put("/api/gamification/showcase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk());

        verify(showcaseService).updateShowcase(any(ShowcaseUpdateRequest.class));
    }
}
