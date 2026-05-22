package id.ac.ui.cs.advprog.yomu.gamification.service.mission;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import id.ac.ui.cs.advprog.yomu.gamification.model.UserDailyMissionProgress;
import id.ac.ui.cs.advprog.yomu.gamification.repository.DailyMissionRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserDailyMissionProgressRepository;
import id.ac.ui.cs.advprog.yomu.gamification.validation.GamificationValidator;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class DailyMissionProgressServiceImplTest {

    // ── Shared data constants ─────────────────────────────────────────────────
    private static final String USERNAME      = "user-42";
    private static final String MISSION_ID    = "mission-1";
    private static final int    TARGET_VALUE  = 5;
    private static final int    REWARD_SCORE  = 100;

    // ── Assertion message constants ───────────────────────────────────────────
    private static final String MSG_NOT_NULL      = "Result should not be null";
    private static final String MSG_PROGRESS_SAVED = "Progress should be saved";
    private static final String MSG_EVENT_PUBLISHED = "Completion event should be published";
    private static final String MSG_NO_EVENT       = "Completion event should not be published";
    private static final String MSG_RESPONSE       = "Response should not be null";
    private static final String MSG_SIZE           = "List size should match";

    @Mock private DailyMissionRepository missionRepository;
    @Mock private UserDailyMissionProgressRepository progressRepository;
    @Mock private GamificationValidator validator;
    @Mock private GamificationMapper mapper;
    @Mock private DailyMissionRotationService rotationService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DailyMissionProgressServiceImpl service;

    private CountBasedDailyMission mission;
    private UserDailyMissionProgress progress;
    private DailyMissionProgressResponse dummyResponse;
    private ProgressUpdateRequest request;

    @BeforeEach
    void setUp() {
        mission = new CountBasedDailyMission();
        mission.setTargetCount(TARGET_VALUE);
        mission.setRewardScore(REWARD_SCORE);

        progress = new UserDailyMissionProgress();
        progress.setUsername(USERNAME);
        progress.setDailyMission(mission);
        progress.setProgressDate(LocalDate.now());
        progress.setProgressValue(0);
        progress.setCompleted(false);

        dummyResponse = new DailyMissionProgressResponse(
                "prog-id", MISSION_ID, USERNAME, LocalDate.now(), 0, false, "quiz_n_times",
                TARGET_VALUE, null, null, REWARD_SCORE, null);

        request = new ProgressUpdateRequest();
        request.setMasterId(MISSION_ID);
        request.setUsername(USERNAME);
        request.setProgressValue(TARGET_VALUE);
    }

    // ─── getOrCreateMissionProgress ──────────────────────────────────────────

    @Test
    void getOrCreateMissionProgress_WhenExisting_ShouldReturnExisting() {
        when(progressRepository.findByUsernameAndDailyMissionAndProgressDate(USERNAME, mission, LocalDate.now()))
                .thenReturn(Optional.of(progress));

        UserDailyMissionProgress result = service.getOrCreateMissionProgress(USERNAME, mission, LocalDate.now());

        assertAll("Verify existing progress is returned",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertEquals(progress, result, "Should return the existing progress object"));
    }

    @Test
    void getOrCreateMissionProgress_WhenNotExisting_ShouldCreateNew() {
        when(progressRepository.findByUsernameAndDailyMissionAndProgressDate(USERNAME, mission, LocalDate.now()))
                .thenReturn(Optional.empty());

        UserDailyMissionProgress result = service.getOrCreateMissionProgress(USERNAME, mission, LocalDate.now());

        assertAll("Verify new progress is created with defaults",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertEquals(USERNAME, result.getUsername(), "Username should be set on new progress"),
                () -> assertEquals(0, result.getProgressValue(), "Initial progress value should be 0"),
                () -> assertEquals(false, result.isCompleted(), "New progress should not be completed"));
    }

    // ─── upsertDailyMissionProgress ──────────────────────────────────────────

    @Test
    void upsertDailyMissionProgress_WhenMissionNotFound_ShouldThrow() {
        doNothing().when(validator).validateMasterId(anyString());
        doNothing().when(validator).validateUsername(anyString());
        when(missionRepository.findById(MISSION_ID)).thenReturn(Optional.empty());

        assertAll("Verify exception when mission not found",
                () -> assertThrows(GamificationException.class,
                        () -> service.upsertDailyMissionProgress(request),
                        "Should throw GamificationException when mission is not found"));
    }

    @Test
    void upsertDailyMissionProgress_WhenProgressReachesTarget_ShouldMarkCompleted() {
        doNothing().when(validator).validateMasterId(anyString());
        doNothing().when(validator).validateUsername(anyString());
        when(missionRepository.findById(MISSION_ID)).thenReturn(Optional.of(mission));
        when(progressRepository.findByUsernameAndDailyMissionAndProgressDate(any(), any(), any()))
                .thenReturn(Optional.of(progress));
        when(progressRepository.save(any())).thenReturn(progress);
        when(mapper.toDailyMissionProgressResponse(any())).thenReturn(dummyResponse);

        DailyMissionProgressResponse result = service.upsertDailyMissionProgress(request);

        assertAll("Verify completed mission marks progress and fires event",
                () -> assertNotNull(result, MSG_RESPONSE),
                () -> assertEquals(true, progress.isCompleted(), "Progress should be marked completed"),
                () -> verify(eventPublisher).publishEvent(any(DailyMissionCompletedEvent.class)));
    }

    @Test
    void upsertDailyMissionProgress_WhenAlreadyCompleted_ShouldNotFireEvent() {
        progress.setCompleted(true);
        request.setProgressValue(TARGET_VALUE + 1);

        doNothing().when(validator).validateMasterId(anyString());
        doNothing().when(validator).validateUsername(anyString());
        when(missionRepository.findById(MISSION_ID)).thenReturn(Optional.of(mission));
        when(progressRepository.findByUsernameAndDailyMissionAndProgressDate(any(), any(), any()))
                .thenReturn(Optional.of(progress));
        when(progressRepository.save(any())).thenReturn(progress);
        when(mapper.toDailyMissionProgressResponse(any())).thenReturn(dummyResponse);

        service.upsertDailyMissionProgress(request);

        assertAll("Verify no event fired when mission was already complete",
                () -> verify(eventPublisher, never()).publishEvent(any(DailyMissionCompletedEvent.class)));
    }

    @Test
    void upsertDailyMissionProgress_WhenProgressBelowTarget_ShouldNotComplete() {
        request.setProgressValue(TARGET_VALUE - 1);

        doNothing().when(validator).validateMasterId(anyString());
        doNothing().when(validator).validateUsername(anyString());
        when(missionRepository.findById(MISSION_ID)).thenReturn(Optional.of(mission));
        when(progressRepository.findByUsernameAndDailyMissionAndProgressDate(any(), any(), any()))
                .thenReturn(Optional.of(progress));
        when(progressRepository.save(any())).thenReturn(progress);
        when(mapper.toDailyMissionProgressResponse(any())).thenReturn(dummyResponse);

        service.upsertDailyMissionProgress(request);

        assertAll("Verify progress below target does not complete the mission",
                () -> assertEquals(false, progress.isCompleted(), "Progress below target should not be completed"),
                () -> verify(eventPublisher, never()).publishEvent(any()));
    }

    // ─── getTodayDailyMissionProgressByUsername ───────────────────────────────

    @Test
    void getTodayDailyMissionProgressByUsername_ShouldReturnMappedList() {
        doNothing().when(validator).validateUsername(anyString());
        when(progressRepository.findByUsernameAndProgressDate(USERNAME, LocalDate.now()))
                .thenReturn(List.of(progress));
        when(mapper.toDailyMissionProgressResponse(progress)).thenReturn(dummyResponse);

        List<DailyMissionProgressResponse> result = service.getTodayDailyMissionProgressByUsername(USERNAME);

        assertAll("Verify today's progress list is returned",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertEquals(1, result.size(), MSG_SIZE));
    }

    // ─── getTodayDailyMissionDashboard ───────────────────────────────────────

    @Test
    void getTodayDailyMissionDashboard_ShouldEnsureRotationAndReturnMissions() {
        doNothing().when(validator).validateUsername(anyString());
        doNothing().when(rotationService).ensureMissionsRotated(any(LocalDate.class));
        when(missionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(any(), any()))
                .thenReturn(List.of(mission));
        when(progressRepository.findByUsernameAndDailyMissionAndProgressDate(any(), any(), any()))
                .thenReturn(Optional.of(progress));
        when(mapper.toDailyMissionProgressResponse(progress)).thenReturn(dummyResponse);

        List<DailyMissionProgressResponse> result = service.getTodayDailyMissionDashboard(USERNAME);

        assertAll("Verify dashboard returns missions after rotation check",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertEquals(1, result.size(), MSG_SIZE),
                () -> verify(rotationService).ensureMissionsRotated(any(LocalDate.class)));
    }

    // ─── saveProgress ─────────────────────────────────────────────────────────

    @Test
    void saveProgress_ShouldDelegateToRepository() {
        when(progressRepository.save(progress)).thenReturn(progress);

        assertAll("Verify save delegates to repository",
                () -> assertDoesNotThrow(() -> service.saveProgress(progress), MSG_PROGRESS_SAVED),
                () -> verify(progressRepository).save(progress));
    }
}
