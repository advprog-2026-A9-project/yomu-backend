package id.ac.ui.cs.advprog.yomu.gamification.controller;

import java.time.LocalDate;
import java.util.List;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementRequest;
import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionRequest;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.ProgressUpdateRequest;
import id.ac.ui.cs.advprog.yomu.gamification.dto.ShowcaseUpdateRequest;
import id.ac.ui.cs.advprog.yomu.gamification.service.achievement.AchievementProgressService;
import id.ac.ui.cs.advprog.yomu.gamification.service.achievement.AchievementService;
import id.ac.ui.cs.advprog.yomu.gamification.service.achievement.AchievementShowcaseService;
import id.ac.ui.cs.advprog.yomu.gamification.service.mission.DailyMissionProgressService;
import id.ac.ui.cs.advprog.yomu.gamification.service.mission.DailyMissionRotationService;
import id.ac.ui.cs.advprog.yomu.gamification.service.mission.DailyMissionService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"null", "PMD"})
class GamificationControllersTest {

    private static final String USERNAME = "wibu-123";
    private static final String ACH_ID = "ach-123";
    private static final String MIS_ID = "mis-123";

    private ObjectMapper objectMapper;

    // --- Mock Services for Showcase ---
    @Mock
    private AchievementShowcaseService showcaseService;
    @InjectMocks
    private AchievementShowcaseController showcaseController;
    private MockMvc showcaseMvc;

    // --- Mock Services for Progress ---
    @Mock
    private AchievementProgressService achievementProgressService;
    @Mock
    private DailyMissionProgressService dailyMissionProgressService;
    @InjectMocks
    private ProgressTrackingController progressTrackingController;
    private MockMvc progressMvc;

    // --- Mock Services for Admin ---
    @Mock
    private AchievementService achievementService;
    @Mock
    private DailyMissionService dailyMissionService;
    @Mock
    private DailyMissionRotationService dailyMissionRotationService;
    @InjectMocks
    private GamificationAdminController adminController;
    private MockMvc adminMvc;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Register Java 8 Date/Time module

        showcaseMvc = MockMvcBuilders.standaloneSetup(showcaseController).build();
        progressMvc = MockMvcBuilders.standaloneSetup(progressTrackingController).build();
        adminMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    // ==========================================
    // AchievementShowcaseController Tests
    // ==========================================
    @Test
    void showcase_GetShowcase_Success() throws Exception {
        when(showcaseService.getShowcaseByUsername(USERNAME)).thenReturn(List.of(ACH_ID));

        showcaseMvc.perform(get("/api/gamification/showcase")
                .param("username", USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(ACH_ID));

        verify(showcaseService, times(1)).getShowcaseByUsername(USERNAME);
    }

    @Test
    void showcase_UpdateShowcase_Success() throws Exception {
        ShowcaseUpdateRequest request = new ShowcaseUpdateRequest();
        request.setUsername(USERNAME);
        request.setAchievementIds(List.of(ACH_ID));

        showcaseMvc.perform(put("/api/gamification/showcase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(showcaseService, times(1)).updateShowcase(any(ShowcaseUpdateRequest.class));
    }

    // ==========================================
    // ProgressTrackingController Tests
    // ==========================================
    @Test
    void progress_UpsertAchievementProgress_Success() throws Exception {
        ProgressUpdateRequest request = new ProgressUpdateRequest();
        request.setUsername(USERNAME);
        request.setMasterId(ACH_ID);
        request.setProgressValue(1);

        AchievementProgressResponse response = new AchievementProgressResponse(
                ACH_ID, "Master", USERNAME, 5, true, "Milestone desc", "count_based", 10, null, "GOLD", null
        );
        when(achievementProgressService.upsertAchievementProgress(any(ProgressUpdateRequest.class))).thenReturn(response);

        progressMvc.perform(post("/api/gamification/progress/achievements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.achievementId").value(ACH_ID))
                .andExpect(jsonPath("$.progressValue").value(5));

        verify(achievementProgressService, times(1)).upsertAchievementProgress(any(ProgressUpdateRequest.class));
    }

    @Test
    void progress_UpsertDailyMissionProgress_Success() throws Exception {
        ProgressUpdateRequest request = new ProgressUpdateRequest();
        request.setUsername(USERNAME);
        request.setMasterId(MIS_ID);
        request.setProgressValue(2);

        DailyMissionProgressResponse response = new DailyMissionProgressResponse(
                MIS_ID, "Read daily", USERNAME, LocalDate.now(), 2, false, "Read 2 articles", 2, null, null, 10, "read_n_articles"
        );
        when(dailyMissionProgressService.upsertDailyMissionProgress(any(ProgressUpdateRequest.class))).thenReturn(response);

        progressMvc.perform(post("/api/gamification/progress/daily-missions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyMissionId").value(MIS_ID))
                .andExpect(jsonPath("$.progressValue").value(2));

        verify(dailyMissionProgressService, times(1)).upsertDailyMissionProgress(any(ProgressUpdateRequest.class));
    }

    @Test
    void progress_GetAchievementProgress_Success() throws Exception {
        AchievementProgressResponse response = new AchievementProgressResponse(
                ACH_ID, "Master", USERNAME, 5, true, "Milestone desc", "count_based", 10, null, "GOLD", null
        );
        when(achievementProgressService.getAchievementProgressByUsername(USERNAME)).thenReturn(List.of(response));

        progressMvc.perform(get("/api/gamification/progress/achievements")
                .param("username", USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].achievementId").value(ACH_ID));

        verify(achievementProgressService, times(1)).getAchievementProgressByUsername(USERNAME);
    }

    @Test
    void progress_GetTodayDailyMissionProgress_Success() throws Exception {
        DailyMissionProgressResponse response = new DailyMissionProgressResponse(
                MIS_ID, "Read daily", USERNAME, LocalDate.now(), 2, false, "Read 2 articles", 2, null, null, 10, "read_n_articles"
        );
        when(dailyMissionProgressService.getTodayDailyMissionProgressByUsername(USERNAME)).thenReturn(List.of(response));

        progressMvc.perform(get("/api/gamification/progress/daily-missions")
                .param("username", USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dailyMissionId").value(MIS_ID));

        verify(dailyMissionProgressService, times(1)).getTodayDailyMissionProgressByUsername(USERNAME);
    }

    @Test
    void progress_GetTodayDailyMissionDashboard_Success() throws Exception {
        DailyMissionProgressResponse response = new DailyMissionProgressResponse(
                MIS_ID, "Read daily", USERNAME, LocalDate.now(), 2, false, "Read 2 articles", 2, null, null, 10, "read_n_articles"
        );
        when(dailyMissionProgressService.getTodayDailyMissionDashboard(USERNAME)).thenReturn(List.of(response));

        progressMvc.perform(get("/api/gamification/progress/daily-missions/today")
                .param("username", USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dailyMissionId").value(MIS_ID));

        verify(dailyMissionProgressService, times(1)).getTodayDailyMissionDashboard(USERNAME);
    }

    // ==========================================
    // GamificationAdminController Tests
    // ==========================================
    @Test
    void admin_CreateAchievement_Success() throws Exception {
        AchievementRequest req = new AchievementRequest();
        req.setName("First Step");
        req.setMilestone("Milestone");
        req.setMilestoneType("count_based");
        req.setMilestoneThreshold(5);

        AchievementResponse resp = new AchievementResponse(
                ACH_ID, "First Step", "Milestone", "count_based", 5, null, "BRONZE", null, 0L, true
        );
        when(achievementService.create(any(AchievementRequest.class))).thenReturn(resp);

        adminMvc.perform(post("/api/gamification/admin/achievements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ACH_ID));

        verify(achievementService, times(1)).create(any(AchievementRequest.class));
    }

    @Test
    void admin_GetAchievements_Success() throws Exception {
        AchievementResponse resp = new AchievementResponse(
                ACH_ID, "First Step", "Milestone", "count_based", 5, null, "BRONZE", null, 0L, true
        );
        when(achievementService.findAll()).thenReturn(List.of(resp));

        adminMvc.perform(get("/api/gamification/admin/achievements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ACH_ID));

        verify(achievementService, times(1)).findAll();
    }

    @Test
    void admin_UpdateAchievement_Success() throws Exception {
        AchievementRequest req = new AchievementRequest();
        req.setName("First Step Updated");
        req.setMilestone("Milestone");
        req.setMilestoneType("count_based");
        req.setMilestoneThreshold(5);

        AchievementResponse resp = new AchievementResponse(
                ACH_ID, "First Step Updated", "Milestone", "count_based", 5, null, "BRONZE", null, 0L, true
        );
        when(achievementService.update(eq(ACH_ID), any(AchievementRequest.class))).thenReturn(resp);

        adminMvc.perform(put("/api/gamification/admin/achievements/" + ACH_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("First Step Updated"));

        verify(achievementService, times(1)).update(eq(ACH_ID), any(AchievementRequest.class));
    }

    @Test
    void admin_DeleteAchievement_Success() throws Exception {
        adminMvc.perform(delete("/api/gamification/admin/achievements/" + ACH_ID))
                .andExpect(status().isNoContent());

        verify(achievementService, times(1)).delete(ACH_ID);
    }

    @Test
    void admin_CreateDailyMission_Success() throws Exception {
        DailyMissionRequest req = new DailyMissionRequest();
        req.setName("Daily Mission 1");
        req.setMilestone("Milestone");
        req.setMissionType("read_n_articles");
        req.setRewardScore(10);

        DailyMissionResponse resp = new DailyMissionResponse(
                MIS_ID, "Daily Mission 1", "Milestone", "read_n_articles", 5, null, null, 10, null, null, true
        );
        when(dailyMissionService.create(any(DailyMissionRequest.class))).thenReturn(resp);

        adminMvc.perform(post("/api/gamification/admin/daily-missions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(MIS_ID));

        verify(dailyMissionService, times(1)).create(any(DailyMissionRequest.class));
    }

    @Test
    void admin_UpdateDailyMission_Success() throws Exception {
        DailyMissionRequest req = new DailyMissionRequest();
        req.setName("Daily Mission Updated");
        req.setMilestone("Milestone");
        req.setMissionType("read_n_articles");
        req.setRewardScore(10);

        DailyMissionResponse resp = new DailyMissionResponse(
                MIS_ID, "Daily Mission Updated", "Milestone", "read_n_articles", 5, null, null, 10, null, null, true
        );
        when(dailyMissionService.update(eq(MIS_ID), any(DailyMissionRequest.class))).thenReturn(resp);

        adminMvc.perform(put("/api/gamification/admin/daily-missions/" + MIS_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Daily Mission Updated"));

        verify(dailyMissionService, times(1)).update(eq(MIS_ID), any(DailyMissionRequest.class));
    }

    @Test
    void admin_DeleteDailyMission_Success() throws Exception {
        adminMvc.perform(delete("/api/gamification/admin/daily-missions/" + MIS_ID))
                .andExpect(status().isNoContent());

        verify(dailyMissionService, times(1)).delete(MIS_ID);
    }

    @Test
    void admin_GetDailyMissions_Success() throws Exception {
        DailyMissionResponse resp = new DailyMissionResponse(
                MIS_ID, "Daily Mission 1", "Milestone", "read_n_articles", 5, null, null, 10, null, null, true
        );
        when(dailyMissionService.findAll()).thenReturn(List.of(resp));

        adminMvc.perform(get("/api/gamification/admin/daily-missions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(MIS_ID));

        verify(dailyMissionService, times(1)).findAll();
    }

    @Test
    void admin_SetTodayMissions_Success() throws Exception {
        List<String> missionIds = List.of(MIS_ID);

        adminMvc.perform(post("/api/gamification/admin/daily-missions/select")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(missionIds)))
                .andExpect(status().isOk());

        verify(dailyMissionRotationService, times(1)).setTodayMissions(missionIds);
    }

    @Test
    void admin_RandomizeTodayMissions_Success() throws Exception {
        adminMvc.perform(post("/api/gamification/admin/daily-missions/randomize"))
                .andExpect(status().isOk());

        verify(dailyMissionRotationService, times(1)).forceRotateMissions();
    }
}
