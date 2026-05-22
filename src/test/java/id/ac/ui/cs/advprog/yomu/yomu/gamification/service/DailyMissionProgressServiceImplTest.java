package id.ac.ui.cs.advprog.yomu.gamification.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.ProgressUpdateRequest;
import id.ac.ui.cs.advprog.yomu.gamification.event.DailyMissionCompletedEvent;
import id.ac.ui.cs.advprog.yomu.gamification.exception.GamificationException;
import id.ac.ui.cs.advprog.yomu.gamification.mapper.GamificationMapper;
import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedDailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserDailyMissionProgress;
import id.ac.ui.cs.advprog.yomu.gamification.repository.DailyMissionRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserDailyMissionProgressRepository;
import id.ac.ui.cs.advprog.yomu.gamification.service.mission.DailyMissionProgressServiceImpl;
import id.ac.ui.cs.advprog.yomu.gamification.service.mission.DailyMissionRotationService;
import id.ac.ui.cs.advprog.yomu.gamification.validation.GamificationValidator;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class DailyMissionProgressServiceImplTest {

    private static final String DM_COUNT = "dm-count";
    private static final String READ_N_ARTICLES = "read_n_articles";
    private static final String READ_3_ARTICLES = "Read 3 Articles";
    private static final String READ_3_ARTICLES_TODAY = "Read 3 articles today";

    @Mock
    private DailyMissionRepository dailyMissionRepository;

    @Mock
    private UserDailyMissionProgressRepository userDailyMissionProgressRepository;

    @Mock
    private GamificationValidator validator;

    @Mock
    private GamificationMapper mapper;

    @Mock
    private DailyMissionRotationService dailyMissionRotationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DailyMissionProgressServiceImpl progressService;

    private String username;
    private DailyMission mission;
    private UserDailyMissionProgress progress;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        username = "user-123";
        today = LocalDate.now();

        CountBasedDailyMission m = new CountBasedDailyMission();
        m.setId(DM_COUNT);
        m.setName(READ_3_ARTICLES);
        m.setMilestone(READ_3_ARTICLES_TODAY);
        m.setMissionType(READ_N_ARTICLES);
        m.setTargetCount(3);
        m.setRewardScore(50);
        mission = m;

        progress = new UserDailyMissionProgress();
        progress.setId("p-123");
        progress.setUsername(username);
        progress.setDailyMission(mission);
        progress.setProgressDate(today);
        progress.setProgressValue(1);
        progress.setCompleted(false);
    }

    @Test
    void getOrCreateMissionProgress_WhenExists_ShouldReturnExisting() {
        when(userDailyMissionProgressRepository.findByUsernameAndDailyMissionAndProgressDate(username, mission, today))
                .thenReturn(Optional.of(progress));

        UserDailyMissionProgress result = progressService.getOrCreateMissionProgress(username, mission, today);

        assertAll("Verify existing progress returned",
                () -> assertNotNull(result, "Result should not be null"),
                () -> assertEquals("p-123", result.getId(), "Progress ID should match"),
                () -> assertEquals(1, result.getProgressValue(), "Progress value should be 1"));
    }

    @Test
    void getOrCreateMissionProgress_WhenDoesNotExist_ShouldReturnNew() {
        when(userDailyMissionProgressRepository.findByUsernameAndDailyMissionAndProgressDate(username, mission, today))
                .thenReturn(Optional.empty());

        UserDailyMissionProgress result = progressService.getOrCreateMissionProgress(username, mission, today);

        assertAll("Verify new progress returned",
                () -> assertNotNull(result, "Result should not be null"),
                () -> assertEquals(username, result.getUsername(), "Username should match"),
                () -> assertEquals(mission, result.getDailyMission(), "Daily mission should match"),
                () -> assertEquals(today, result.getProgressDate(), "Progress date should be today"),
                () -> assertEquals(0, result.getProgressValue(), "Progress value should default to 0"),
                () -> assertFalse(result.isCompleted(), "Completed flag should be false"));
    }

    @Test
    void upsertDailyMissionProgress_WhenCompletedNew_ShouldPublishEventAndSave() {
        ProgressUpdateRequest request = new ProgressUpdateRequest();
        request.setUsername(username);
        request.setMasterId(DM_COUNT);
        request.setProgressValue(3);

        doNothing().when(validator).validateMasterId(DM_COUNT);
        doNothing().when(validator).validateUsername(username);
        when(dailyMissionRepository.findById(DM_COUNT)).thenReturn(Optional.of(mission));
        when(userDailyMissionProgressRepository.findByUsernameAndDailyMissionAndProgressDate(username, mission, today))
                .thenReturn(Optional.of(progress));
        when(userDailyMissionProgressRepository.save(any(UserDailyMissionProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyMissionProgressResponse expectedResponse = new DailyMissionProgressResponse(
                DM_COUNT, READ_3_ARTICLES, username, today, 3, true,
                READ_3_ARTICLES_TODAY, 3, null, null, 50, READ_N_ARTICLES
        );
        when(mapper.toDailyMissionProgressResponse(any(UserDailyMissionProgress.class))).thenReturn(expectedResponse);

        DailyMissionProgressResponse response = progressService.upsertDailyMissionProgress(request);

        assertAll("Verify completion state changes and mock interactions",
                () -> assertNotNull(response, "Response should not be null"),
                () -> assertEquals(3, response.progressValue(), "Progress value should match request"),
                () -> assertTrue(response.completed(), "Completed flag should be true in response"),
                () -> verify(userDailyMissionProgressRepository).save(progress),
                () -> verify(eventPublisher).publishEvent(any(DailyMissionCompletedEvent.class)),
                () -> assertTrue(progress.isCompleted(), "Progress entity isCompleted flag should be true"),
                () -> assertNotNull(progress.getCompletedAt(), "CompletedAt timestamp should not be null"));
    }

    @Test
    void upsertDailyMissionProgress_WhenAlreadyCompleted_ShouldNotPublishEvent() {
        ProgressUpdateRequest request = new ProgressUpdateRequest();
        request.setUsername(username);
        request.setMasterId(DM_COUNT);
        request.setProgressValue(4);

        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now().minusHours(1));

        doNothing().when(validator).validateMasterId(DM_COUNT);
        doNothing().when(validator).validateUsername(username);
        when(dailyMissionRepository.findById(DM_COUNT)).thenReturn(Optional.of(mission));
        when(userDailyMissionProgressRepository.findByUsernameAndDailyMissionAndProgressDate(username, mission, today))
                .thenReturn(Optional.of(progress));
        when(userDailyMissionProgressRepository.save(any(UserDailyMissionProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        progressService.upsertDailyMissionProgress(request);

        verify(eventPublisher, never()).publishEvent(any(DailyMissionCompletedEvent.class));
    }

    @Test
    void upsertDailyMissionProgress_WhenNotFound_ShouldThrowException() {
        ProgressUpdateRequest request = new ProgressUpdateRequest();
        request.setUsername(username);
        request.setMasterId("dm-invalid");
        request.setProgressValue(3);

        doNothing().when(validator).validateMasterId("dm-invalid");
        doNothing().when(validator).validateUsername(username);
        when(dailyMissionRepository.findById("dm-invalid")).thenReturn(Optional.empty());

        assertAll("Verify exception when mission not found",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> progressService.upsertDailyMissionProgress(request),
                            "Should throw GamificationException");
                    assertEquals("NOT_FOUND", ex.getErrorCode(), "ErrorCode should be NOT_FOUND");
                });
    }

    @Test
    void getTodayDailyMissionProgressByUsername_ShouldReturnProgressList() {
        doNothing().when(validator).validateUsername(username);
        when(userDailyMissionProgressRepository.findByUsernameAndProgressDate(username, today)).thenReturn(List.of(progress));

        DailyMissionProgressResponse expectedResponse = new DailyMissionProgressResponse(
                DM_COUNT, READ_3_ARTICLES, username, today, 1, false,
                READ_3_ARTICLES_TODAY, 3, null, null, 50, READ_N_ARTICLES
        );
        when(mapper.toDailyMissionProgressResponse(progress)).thenReturn(expectedResponse);

        List<DailyMissionProgressResponse> result = progressService.getTodayDailyMissionProgressByUsername(username);

        assertAll("Verify progress list response contents",
                () -> assertEquals(1, result.size(), "List size should be 1"),
                () -> assertEquals(DM_COUNT, result.get(0).dailyMissionId(), "Daily mission ID should match DM_COUNT"));
    }

    @Test
    void getTodayDailyMissionDashboard_ShouldEnsureRotationAndReturnList() {
        doNothing().when(validator).validateUsername(username);
        doNothing().when(dailyMissionRotationService).ensureMissionsRotated(today);
        when(dailyMissionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(today, today))
                .thenReturn(List.of(mission));
        when(userDailyMissionProgressRepository.findByUsernameAndDailyMissionAndProgressDate(username, mission, today))
                .thenReturn(Optional.of(progress));

        DailyMissionProgressResponse expectedResponse = new DailyMissionProgressResponse(
                DM_COUNT, READ_3_ARTICLES, username, today, 1, false,
                READ_3_ARTICLES_TODAY, 3, null, null, 50, READ_N_ARTICLES
        );
        when(mapper.toDailyMissionProgressResponse(progress)).thenReturn(expectedResponse);

        List<DailyMissionProgressResponse> result = progressService.getTodayDailyMissionDashboard(username);

        assertAll("Verify dashboard contents and rotation initialization",
                () -> assertEquals(1, result.size(), "List size should be 1"),
                () -> verify(dailyMissionRotationService).ensureMissionsRotated(today));
    }

    @Test
    void saveProgress_ShouldCallRepository() {
        progressService.saveProgress(progress);
        verify(userDailyMissionProgressRepository).save(progress);
    }
}
