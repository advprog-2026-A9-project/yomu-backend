package id.ac.ui.cs.advprog.yomu.gamification.controller;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.ProgressUpdateRequest;
import id.ac.ui.cs.advprog.yomu.gamification.service.achievement.AchievementProgressService;
import id.ac.ui.cs.advprog.yomu.gamification.service.mission.DailyMissionProgressService;

@WebMvcTest(ProgressTrackingController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("null")
class ProgressTrackingControllerTest {

    private static final String ACH_1 = "ach-1";
    private static final String NAME = "Name";
    private static final String USER_1 = "user-1";
    private static final String MILESTONE = "milestone";
    private static final String DM_1 = "dm-1";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AchievementProgressService achievementProgressService;

    @MockitoBean
    private DailyMissionProgressService dailyMissionProgressService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void upsertAchievementProgress_ShouldReturnResponse() throws Exception {
        AchievementProgressResponse progress = new AchievementProgressResponse(
                ACH_1, NAME, USER_1, 5, true,
                MILESTONE, "count_based", 5, 0, "Bronze", null
        );
        when(achievementProgressService.upsertAchievementProgress(any(ProgressUpdateRequest.class)))
                .thenReturn(progress);

        String requestJson = String.format("""
                {
                    "username": "%s",
                    "masterId": "%s",
                    "progressValue": 5
                }
                """, USER_1, ACH_1);

        mockMvc.perform(post("/api/gamification/progress/achievements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.achievementId").value(ACH_1))
                .andExpect(jsonPath("$.unlocked").value(true));

        verify(achievementProgressService).upsertAchievementProgress(any(ProgressUpdateRequest.class));
    }

    @Test
    void upsertDailyMissionProgress_ShouldReturnResponse() throws Exception {
        DailyMissionProgressResponse progress = new DailyMissionProgressResponse(
                DM_1, NAME, USER_1, LocalDate.now(), 3, true,
                MILESTONE, 3, null, null, 50, "read_n_articles"
        );
        when(dailyMissionProgressService.upsertDailyMissionProgress(any(ProgressUpdateRequest.class)))
                .thenReturn(progress);

        String requestJson = String.format("""
                {
                    "username": "%s",
                    "masterId": "%s",
                    "progressValue": 3
                }
                """, USER_1, DM_1);

        mockMvc.perform(post("/api/gamification/progress/daily-missions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyMissionId").value(DM_1))
                .andExpect(jsonPath("$.completed").value(true));

        verify(dailyMissionProgressService).upsertDailyMissionProgress(any(ProgressUpdateRequest.class));
    }

    @Test
    void getAchievementProgress_ShouldReturnList() throws Exception {
        AchievementProgressResponse progress = new AchievementProgressResponse(
                ACH_1, NAME, USER_1, 5, true,
                MILESTONE, "count_based", 5, 0, "Bronze", null
        );
        when(achievementProgressService.getAchievementProgressByUsername(USER_1))
                .thenReturn(List.of(progress));

        mockMvc.perform(get("/api/gamification/progress/achievements").param("username", USER_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].achievementId").value(ACH_1));

        verify(achievementProgressService).getAchievementProgressByUsername(USER_1);
    }

    @Test
    void getTodayDailyMissionProgress_ShouldReturnList() throws Exception {
        DailyMissionProgressResponse progress = new DailyMissionProgressResponse(
                DM_1, NAME, USER_1, LocalDate.now(), 1, false,
                MILESTONE, 3, null, null, 50, "read_n_articles"
        );
        when(dailyMissionProgressService.getTodayDailyMissionProgressByUsername(USER_1))
                .thenReturn(List.of(progress));

        mockMvc.perform(get("/api/gamification/progress/daily-missions").param("username", USER_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dailyMissionId").value(DM_1));

        verify(dailyMissionProgressService).getTodayDailyMissionProgressByUsername(USER_1);
    }

    @Test
    void getTodayDailyMissionDashboard_ShouldReturnList() throws Exception {
        DailyMissionProgressResponse progress = new DailyMissionProgressResponse(
                DM_1, NAME, USER_1, LocalDate.now(), 1, false,
                MILESTONE, 3, null, null, 50, "read_n_articles"
        );
        when(dailyMissionProgressService.getTodayDailyMissionDashboard(USER_1))
                .thenReturn(List.of(progress));

        mockMvc.perform(get("/api/gamification/progress/daily-missions/today").param("username", USER_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dailyMissionId").value(DM_1));

        verify(dailyMissionProgressService).getTodayDailyMissionDashboard(USER_1);
    }
}
