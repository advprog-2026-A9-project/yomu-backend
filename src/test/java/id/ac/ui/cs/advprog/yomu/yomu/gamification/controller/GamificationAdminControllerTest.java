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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementRequest;
import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionRequest;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionResponse;
import id.ac.ui.cs.advprog.yomu.gamification.service.achievement.AchievementService;
import id.ac.ui.cs.advprog.yomu.gamification.service.mission.DailyMissionRotationService;
import id.ac.ui.cs.advprog.yomu.gamification.service.mission.DailyMissionService;

@WebMvcTest(GamificationAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("null")
class GamificationAdminControllerTest {

    private static final String ACH_1 = "ach-1";
    private static final String NAME = "Name";
    private static final String MILESTONE = "milestone";
    private static final String NEW_NAME = "New Name";
    private static final String DM_1 = "dm-1";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AchievementService achievementService;

    @MockitoBean
    private DailyMissionService dailyMissionService;

    @MockitoBean
    private DailyMissionRotationService dailyMissionRotationService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void createAchievement_ShouldReturnCreated() throws Exception {
        AchievementResponse response = new AchievementResponse(
                ACH_1, NAME, MILESTONE, "count_based", 5, 0, "Bronze", null, 0, true
        );
        when(achievementService.create(any(AchievementRequest.class))).thenReturn(response);

        String requestJson = String.format("""
                {
                    "name": "%s",
                    "milestone": "%s",
                    "milestoneType": "count_based",
                    "milestoneThreshold": 5,
                    "tier": "Bronze"
                }
                """, NAME, MILESTONE);

        mockMvc.perform(post("/api/gamification/admin/achievements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ACH_1));

        verify(achievementService).create(any(AchievementRequest.class));
    }

    @Test
    void getAchievements_ShouldReturnList() throws Exception {
        AchievementResponse response = new AchievementResponse(
                ACH_1, NAME, MILESTONE, "count_based", 5, 0, "Bronze", null, 0, true
        );
        when(achievementService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/gamification/admin/achievements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ACH_1));

        verify(achievementService).findAll();
    }

    @Test
    void updateAchievement_ShouldReturnUpdated() throws Exception {
        AchievementResponse response = new AchievementResponse(
                ACH_1, NEW_NAME, MILESTONE, "count_based", 5, 0, "Bronze", null, 0, true
        );
        when(achievementService.update(eq(ACH_1), any(AchievementRequest.class))).thenReturn(response);

        String requestJson = String.format("""
                {
                    "name": "%s",
                    "milestone": "%s",
                    "milestoneType": "count_based",
                    "milestoneThreshold": 5,
                    "tier": "Bronze"
                }
                """, NEW_NAME, MILESTONE);

        mockMvc.perform(put("/api/gamification/admin/achievements/" + ACH_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(NEW_NAME));

        verify(achievementService).update(eq(ACH_1), any(AchievementRequest.class));
    }

    @Test
    void deleteAchievement_ShouldReturnNoContent() throws Exception {
        doNothing().when(achievementService).delete(ACH_1);

        mockMvc.perform(delete("/api/gamification/admin/achievements/" + ACH_1))
                .andExpect(status().isNoContent());

        verify(achievementService).delete(ACH_1);
    }

    @Test
    void createDailyMission_ShouldReturnCreated() throws Exception {
        DailyMissionResponse response = new DailyMissionResponse(
                DM_1, NAME, MILESTONE, "read_n_articles", 3, null, null, 50, LocalDate.now(), LocalDate.now(), true
        );
        when(dailyMissionService.create(any(DailyMissionRequest.class))).thenReturn(response);

        String requestJson = String.format("""
                {
                    "name": "%s",
                    "milestone": "%s",
                    "missionType": "read_n_articles",
                    "targetCount": 3,
                    "rewardScore": 50
                }
                """, NAME, MILESTONE);

        mockMvc.perform(post("/api/gamification/admin/daily-missions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(DM_1));

        verify(dailyMissionService).create(any(DailyMissionRequest.class));
    }

    @Test
    void updateDailyMission_ShouldReturnUpdated() throws Exception {
        DailyMissionResponse response = new DailyMissionResponse(
                DM_1, NEW_NAME, MILESTONE, "read_n_articles", 3, null, null, 50, LocalDate.now(), LocalDate.now(), true
        );
        when(dailyMissionService.update(eq(DM_1), any(DailyMissionRequest.class))).thenReturn(response);

        String requestJson = String.format("""
                {
                    "name": "%s",
                    "milestone": "%s",
                    "missionType": "read_n_articles",
                    "targetCount": 3,
                    "rewardScore": 50
                }
                """, NEW_NAME, MILESTONE);

        mockMvc.perform(put("/api/gamification/admin/daily-missions/" + DM_1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(NEW_NAME));

        verify(dailyMissionService).update(eq(DM_1), any(DailyMissionRequest.class));
    }

    @Test
    void deleteDailyMission_ShouldReturnNoContent() throws Exception {
        doNothing().when(dailyMissionService).delete(DM_1);

        mockMvc.perform(delete("/api/gamification/admin/daily-missions/" + DM_1))
                .andExpect(status().isNoContent());

        verify(dailyMissionService).delete(DM_1);
    }

    @Test
    void getDailyMissions_ShouldReturnList() throws Exception {
        DailyMissionResponse response = new DailyMissionResponse(
                DM_1, NAME, MILESTONE, "read_n_articles", 3, null, null, 50, LocalDate.now(), LocalDate.now(), true
        );
        when(dailyMissionService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/gamification/admin/daily-missions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(DM_1));

        verify(dailyMissionService).findAll();
    }

    @Test
    void setTodayMissions_ShouldReturnOk() throws Exception {
        doNothing().when(dailyMissionRotationService).setTodayMissions(any());

        mockMvc.perform(post("/api/gamification/admin/daily-missions/select")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"" + DM_1 + "\", \"dm-2\", \"dm-3\"]"))
                .andExpect(status().isOk());

        verify(dailyMissionRotationService).setTodayMissions(any());
    }

    @Test
    void randomizeTodayMissions_ShouldReturnOk() throws Exception {
        doNothing().when(dailyMissionRotationService).forceRotateMissions();

        mockMvc.perform(post("/api/gamification/admin/daily-missions/randomize"))
                .andExpect(status().isOk());

        verify(dailyMissionRotationService).forceRotateMissions();
    }
}
