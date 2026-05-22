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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionRequest;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionResponse;
import id.ac.ui.cs.advprog.yomu.gamification.exception.GamificationException;
import id.ac.ui.cs.advprog.yomu.gamification.mapper.GamificationMapper;
import id.ac.ui.cs.advprog.yomu.gamification.model.AccuracyDailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedDailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.repository.DailyMissionRepository;
import id.ac.ui.cs.advprog.yomu.gamification.validation.GamificationValidator;

@ExtendWith(MockitoExtension.class)
class DailyMissionServiceImplTest {

    // ── Shared data constants ─────────────────────────────────────────────────
    private static final String MISSION_ID      = "mission-99";
    private static final String MISSION_NAME    = "Quiz Champion";
    private static final String MISSION_STONE   = "Complete 5 quizzes";
    private static final String TYPE_COUNT      = "count_based";
    private static final String TYPE_ACCURACY   = "achieve_accuracy";

    // ── Assertion message constants ───────────────────────────────────────────
    private static final String MSG_NOT_NULL    = "Response should not be null";
    private static final String MSG_SAVED       = "Mission should be saved to repository";
    private static final String MSG_DELETED     = "Mission should be deleted from repository";
    private static final String MSG_THROW       = "Should throw GamificationException";
    private static final String MSG_CODE_DUP    = "Error code should be DUPLICATE_NAME";
    private static final String MSG_CODE_404    = "Error code should be NOT_FOUND";
    private static final String MSG_CODE_TYPE   = "Error code should be INVALID_TYPE_CHANGE";
    private static final String MSG_LIST_SIZE   = "findAll list size should match";

    @Mock private DailyMissionRepository missionRepository;
    @Mock private GamificationValidator validator;
    @Mock private GamificationMapper mapper;

    @InjectMocks
    private DailyMissionServiceImpl service;

    private DailyMissionRequest countRequest;
    private DailyMissionRequest accuracyRequest;
    private CountBasedDailyMission savedCountMission;
    private AccuracyDailyMission savedAccuracyMission;
    private DailyMissionResponse dummyResponse;

    @BeforeEach
    void setUp() {
        countRequest = new DailyMissionRequest();
        countRequest.setName(MISSION_NAME);
        countRequest.setMilestone(MISSION_STONE);
        countRequest.setMissionType(TYPE_COUNT);
        countRequest.setTargetCount(5);
        countRequest.setRewardScore(100);
        countRequest.setActiveFrom(LocalDate.now());
        countRequest.setActiveUntil(LocalDate.now().plusDays(1));

        accuracyRequest = new DailyMissionRequest();
        accuracyRequest.setName(MISSION_NAME + " Accuracy");
        accuracyRequest.setMilestone(MISSION_STONE);
        accuracyRequest.setMissionType(TYPE_ACCURACY);
        accuracyRequest.setAccuracyThreshold(80);
        accuracyRequest.setRequiredCount(3);
        accuracyRequest.setRewardScore(150);

        savedCountMission = new CountBasedDailyMission();
        savedCountMission.setName(MISSION_NAME);
        savedCountMission.setMilestone(MISSION_STONE);
        savedCountMission.setMissionType(TYPE_COUNT);
        savedCountMission.setTargetCount(5);
        savedCountMission.setRewardScore(100);

        savedAccuracyMission = new AccuracyDailyMission();
        savedAccuracyMission.setName(MISSION_NAME + " Accuracy");
        savedAccuracyMission.setMilestone(MISSION_STONE);
        savedAccuracyMission.setMissionType(TYPE_ACCURACY);
        savedAccuracyMission.setAccuracyThreshold(80);
        savedAccuracyMission.setRequiredCount(3);
        savedAccuracyMission.setRewardScore(150);

        dummyResponse = new DailyMissionResponse(
            MISSION_ID, MISSION_NAME, MISSION_STONE, TYPE_COUNT, 5, null, null, 100, null, null, true);
    }

    // ─── create ──────────────────────────────────────────────────────────────

    @Test
    void create_WhenCountBased_ShouldSaveAndReturnResponse() {
        doNothing().when(validator).validateDailyMissionRequest(any());
        when(missionRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(missionRepository.save(any())).thenReturn(savedCountMission);
        when(mapper.toDailyMissionResponse(savedCountMission)).thenReturn(dummyResponse);

        DailyMissionResponse result = service.create(countRequest);

        assertAll("Verify count-based mission is created",
                () -> assertNotNull(result, MSG_NOT_NULL),
            () -> verify(missionRepository).save(any()));
    }

    @Test
    void create_WhenAccuracyBased_ShouldSaveAndReturnResponse() {
        doNothing().when(validator).validateDailyMissionRequest(any());
        when(missionRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(missionRepository.save(any())).thenReturn(savedAccuracyMission);
        when(mapper.toDailyMissionResponse(savedAccuracyMission)).thenReturn(dummyResponse);

        DailyMissionResponse result = service.create(accuracyRequest);

        assertAll("Verify accuracy-based mission is created",
                () -> assertNotNull(result, MSG_NOT_NULL),
            () -> verify(missionRepository).save(any()));
    }

    @Test
    void create_WhenDuplicateName_ShouldThrow() {
        doNothing().when(validator).validateDailyMissionRequest(any());
        when(missionRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(savedCountMission));

        assertAll("Verify duplicate name throws on create",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> service.create(countRequest), MSG_THROW);
                    assertEquals("DUPLICATE_NAME", ex.getErrorCode(), MSG_CODE_DUP);
                });
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Test
    void update_WhenMissionNotFound_ShouldThrow() {
        doNothing().when(validator).validateDailyMissionRequest(any());
        doNothing().when(validator).validateMasterId(anyString());
        when(missionRepository.findById(MISSION_ID)).thenReturn(Optional.empty());

        assertAll("Verify exception when updating non-existent mission",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> service.update(MISSION_ID, countRequest), MSG_THROW);
                    assertEquals("NOT_FOUND", ex.getErrorCode(), MSG_CODE_404);
                });
    }

    @Test
    void update_WhenTypeChanges_ShouldThrow() {
        // Existing is accuracy; request is count-based → type change
        doNothing().when(validator).validateDailyMissionRequest(any());
        doNothing().when(validator).validateMasterId(anyString());
        when(missionRepository.findById(MISSION_ID)).thenReturn(Optional.of(savedAccuracyMission));
        when(missionRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());

        assertAll("Verify type change throws on update",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> service.update(MISSION_ID, countRequest), MSG_THROW);
                    assertEquals("INVALID_TYPE_CHANGE", ex.getErrorCode(), MSG_CODE_TYPE);
                });
    }

    @Test
    void update_WhenDuplicateName_ShouldThrow() {
        CountBasedDailyMission other = new CountBasedDailyMission();
        other.setId("other-mission");
        other.setName("Other Mission");
        // findByNameIgnoreCase returns a *different* mission's id
        doNothing().when(validator).validateDailyMissionRequest(any());
        doNothing().when(validator).validateMasterId(anyString());
        when(missionRepository.findById(MISSION_ID)).thenReturn(Optional.of(savedCountMission));
        when(missionRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(other));

        assertAll("Verify duplicate name on update throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> service.update(MISSION_ID, countRequest), MSG_THROW);
                    assertEquals("DUPLICATE_NAME", ex.getErrorCode(), MSG_CODE_DUP);
                });
    }

    @Test
    void update_WhenValid_ShouldSaveAndReturn() {
        doNothing().when(validator).validateDailyMissionRequest(any());
        doNothing().when(validator).validateMasterId(anyString());
        when(missionRepository.findById(MISSION_ID)).thenReturn(Optional.of(savedCountMission));
        when(missionRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(missionRepository.save(any())).thenReturn(savedCountMission);
        when(mapper.toDailyMissionResponse(any())).thenReturn(dummyResponse);

        DailyMissionResponse result = service.update(MISSION_ID, countRequest);

        assertAll("Verify valid update saves and returns response",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> verify(missionRepository).save(savedCountMission));
    }

    @Test
    void create_WhenAccuracyBasedWithNullThresholds_ShouldSaveWithDefaults() {
        accuracyRequest.setAccuracyThreshold(null);
        accuracyRequest.setRequiredCount(null);
        accuracyRequest.setActiveFrom(null);
        accuracyRequest.setActiveUntil(null);

        doNothing().when(validator).validateDailyMissionRequest(any());
        when(missionRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(missionRepository.save(any())).thenReturn(savedAccuracyMission);
        when(mapper.toDailyMissionResponse(savedAccuracyMission)).thenReturn(dummyResponse);

        DailyMissionResponse result = service.create(accuracyRequest);

        assertAll("Verify accuracy-based mission with null parameters is created",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> verify(missionRepository).save(any()));
    }

    @Test
    void create_WhenCountBasedWithNullTargetCount_ShouldSaveWithDefaults() {
        countRequest.setTargetCount(null);
        countRequest.setActiveFrom(null);
        countRequest.setActiveUntil(null);

        doNothing().when(validator).validateDailyMissionRequest(any());
        when(missionRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(missionRepository.save(any())).thenReturn(savedCountMission);
        when(mapper.toDailyMissionResponse(savedCountMission)).thenReturn(dummyResponse);

        DailyMissionResponse result = service.create(countRequest);

        assertAll("Verify count-based mission with null target count is created",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> verify(missionRepository).save(any()));
    }

    @Test
    void update_WhenAccuracyBasedValid_ShouldSaveAndReturn() {
        doNothing().when(validator).validateDailyMissionRequest(any());
        doNothing().when(validator).validateMasterId(anyString());
        when(missionRepository.findById(MISSION_ID)).thenReturn(Optional.of(savedAccuracyMission));
        when(missionRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(missionRepository.save(any())).thenReturn(savedAccuracyMission);
        when(mapper.toDailyMissionResponse(any())).thenReturn(dummyResponse);

        DailyMissionResponse result = service.update(MISSION_ID, accuracyRequest);

        assertAll("Verify valid accuracy update saves and returns response",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> verify(missionRepository).save(savedAccuracyMission));
    }

    @Test
    void update_WhenAccuracyBasedWithNullThresholds_ShouldSaveWithDefaults() {
        accuracyRequest.setAccuracyThreshold(null);
        accuracyRequest.setRequiredCount(null);
        accuracyRequest.setActiveFrom(null);
        accuracyRequest.setActiveUntil(null);

        doNothing().when(validator).validateDailyMissionRequest(any());
        doNothing().when(validator).validateMasterId(anyString());
        when(missionRepository.findById(MISSION_ID)).thenReturn(Optional.of(savedAccuracyMission));
        when(missionRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(missionRepository.save(any())).thenReturn(savedAccuracyMission);
        when(mapper.toDailyMissionResponse(any())).thenReturn(dummyResponse);

        DailyMissionResponse result = service.update(MISSION_ID, accuracyRequest);

        assertAll("Verify valid accuracy update with null parameters saves and returns response",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> verify(missionRepository).save(savedAccuracyMission));
    }

    @Test
    void update_WhenCountBasedWithNullTargetCount_ShouldSaveWithDefaults() {
        countRequest.setTargetCount(null);
        countRequest.setActiveFrom(null);
        countRequest.setActiveUntil(null);

        doNothing().when(validator).validateDailyMissionRequest(any());
        doNothing().when(validator).validateMasterId(anyString());
        when(missionRepository.findById(MISSION_ID)).thenReturn(Optional.of(savedCountMission));
        when(missionRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(missionRepository.save(any())).thenReturn(savedCountMission);
        when(mapper.toDailyMissionResponse(any())).thenReturn(dummyResponse);

        DailyMissionResponse result = service.update(MISSION_ID, countRequest);

        assertAll("Verify valid count update with null target count saves and returns response",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> verify(missionRepository).save(savedCountMission));
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Test
    void delete_WhenMissionNotFound_ShouldThrow() {
        doNothing().when(validator).validateMasterId(anyString());
        when(missionRepository.findById(MISSION_ID)).thenReturn(Optional.empty());

        assertAll("Verify exception when deleting non-existent mission",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> service.delete(MISSION_ID), MSG_THROW);
                    assertEquals("NOT_FOUND", ex.getErrorCode(), MSG_CODE_404);
                });
    }

    @Test
    void delete_WhenValid_ShouldDeleteMission() {
        doNothing().when(validator).validateMasterId(anyString());
        when(missionRepository.findById(MISSION_ID)).thenReturn(Optional.of(savedCountMission));

        assertAll("Verify valid delete removes mission from repository",
                () -> assertDoesNotThrow(() -> service.delete(MISSION_ID), "Should delete without throwing"),
            () -> verify(missionRepository).delete(savedCountMission));
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Test
    void findAll_ShouldReturnMappedList() {
        when(missionRepository.findAll()).thenReturn(List.of(savedCountMission, savedAccuracyMission));
        when(mapper.toDailyMissionResponse(any())).thenReturn(dummyResponse);

        List<DailyMissionResponse> result = service.findAll();

        assertAll("Verify findAll returns mapped list",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertEquals(2, result.size(), MSG_LIST_SIZE));
    }

    @Test
    void findAll_WhenEmpty_ShouldReturnEmptyList() {
        when(missionRepository.findAll()).thenReturn(List.of());

        List<DailyMissionResponse> result = service.findAll();

        assertAll("Verify findAll returns empty list when no missions",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertEquals(0, result.size(), MSG_LIST_SIZE));
    }
}
