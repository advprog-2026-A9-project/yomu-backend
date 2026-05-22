package id.ac.ui.cs.advprog.yomu.gamification.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
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
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.repository.DailyMissionRepository;
import id.ac.ui.cs.advprog.yomu.gamification.service.mission.DailyMissionServiceImpl;
import id.ac.ui.cs.advprog.yomu.gamification.validation.GamificationValidator;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class DailyMissionServiceImplTest {

    private static final String DM_123 = "dm-123";
    private static final String DM_1 = "dm-1";
    private static final String READ_N_ARTICLES = "read_n_articles";
    private static final String ACHIEVE_ACCURACY = "achieve_accuracy";
    private static final String MILESTONE = "Milestone";
    private static final String OLD_NAME = "Old Name";
    private static final String SHOULD_THROW = "Should throw GamificationException";

    @Mock
    private DailyMissionRepository dailyMissionRepository;

    @Mock
    private GamificationValidator validator;

    @Mock
    private GamificationMapper mapper;

    @InjectMocks
    private DailyMissionServiceImpl dailyMissionService;

    private DailyMissionRequest countRequest;
    private DailyMissionRequest accuracyRequest;

    @BeforeEach
    void setUp() {
        countRequest = new DailyMissionRequest();
        countRequest.setName("Read 3 Articles");
        countRequest.setMilestone("Read 3 articles today");
        countRequest.setMissionType(READ_N_ARTICLES);
        countRequest.setTargetCount(3);
        countRequest.setRewardScore(50);
        countRequest.setActiveFrom(LocalDate.now());
        countRequest.setActiveUntil(LocalDate.now().plusDays(1));

        accuracyRequest = new DailyMissionRequest();
        accuracyRequest.setName("Perfect Accuracy");
        accuracyRequest.setMilestone("Complete 1 quiz with 100% accuracy");
        accuracyRequest.setMissionType(ACHIEVE_ACCURACY);
        accuracyRequest.setAccuracyThreshold(100);
        accuracyRequest.setRequiredCount(1);
        accuracyRequest.setRewardScore(100);
        accuracyRequest.setActiveFrom(LocalDate.now());
        accuracyRequest.setActiveUntil(LocalDate.now().plusDays(1));
    }

    @Test
    void create_WhenCountBased_ShouldSaveAndReturnResponse() {
        doNothing().when(validator).validateDailyMissionRequest(countRequest);
        when(dailyMissionRepository.findByNameIgnoreCase(countRequest.getName())).thenReturn(Optional.empty());
        when(dailyMissionRepository.save(any(DailyMission.class))).thenAnswer(invocation -> {
            DailyMission m = invocation.getArgument(0);
            m.setId("dm-count");
            return m;
        });

        DailyMissionResponse expectedResponse = new DailyMissionResponse(
                "dm-count", "Read 3 Articles", "Read 3 articles today", READ_N_ARTICLES,
                3, null, null, 50, LocalDate.now(), LocalDate.now().plusDays(1), true
        );
        when(mapper.toDailyMissionResponse(any(DailyMission.class))).thenReturn(expectedResponse);

        DailyMissionResponse response = dailyMissionService.create(countRequest);

        assertAll("Verify created count-based mission",
                () -> assertNotNull(response, "Response should not be null"),
                () -> assertEquals("dm-count", response.id(), "Mission ID should match"),
                () -> assertEquals(3, response.targetCount(), "Target count should be 3"));
    }

    @Test
    void create_WhenAccuracyBased_ShouldSaveAndReturnResponse() {
        doNothing().when(validator).validateDailyMissionRequest(accuracyRequest);
        when(dailyMissionRepository.findByNameIgnoreCase(accuracyRequest.getName())).thenReturn(Optional.empty());
        when(dailyMissionRepository.save(any(DailyMission.class))).thenAnswer(invocation -> {
            DailyMission m = invocation.getArgument(0);
            m.setId("dm-accuracy");
            return m;
        });

        DailyMissionResponse expectedResponse = new DailyMissionResponse(
                "dm-accuracy", "Perfect Accuracy", "Complete 1 quiz with 100% accuracy", ACHIEVE_ACCURACY,
                null, 100, 1, 100, LocalDate.now(), LocalDate.now().plusDays(1), true
        );
        when(mapper.toDailyMissionResponse(any(DailyMission.class))).thenReturn(expectedResponse);

        DailyMissionResponse response = dailyMissionService.create(accuracyRequest);

        assertAll("Verify created accuracy-based mission",
                () -> assertNotNull(response, "Response should not be null"),
                () -> assertEquals("dm-accuracy", response.id(), "Mission ID should match"),
                () -> assertEquals(100, response.accuracyThreshold(), "Accuracy threshold should be 100"));
    }

    @Test
    void create_WhenAccuracyBasedWithNullThresholdAndCount_ShouldUseDefaults() {
        DailyMissionRequest nullAccRequest = new DailyMissionRequest();
        nullAccRequest.setName("Perfect Accuracy Default");
        nullAccRequest.setMilestone(MILESTONE);
        nullAccRequest.setMissionType(ACHIEVE_ACCURACY);
        nullAccRequest.setRewardScore(100);

        doNothing().when(validator).validateDailyMissionRequest(nullAccRequest);
        when(dailyMissionRepository.findByNameIgnoreCase(nullAccRequest.getName())).thenReturn(Optional.empty());
        when(dailyMissionRepository.save(any(DailyMission.class))).thenAnswer(invocation -> {
            AccuracyDailyMission m = invocation.getArgument(0);
            assertAll("Verify default parameters for accuracy mission",
                    () -> assertEquals(0, m.getAccuracyThreshold(), "Accuracy threshold should default to 0"),
                    () -> assertEquals(1, m.getRequiredCount(), "Required count should default to 1"),
                    () -> assertEquals(LocalDate.now(), m.getActiveFrom(), "ActiveFrom should default to today"),
                    () -> assertEquals(LocalDate.now().plusDays(1), m.getActiveUntil(), "ActiveUntil should default to tomorrow"));
            m.setId("dm-accuracy-null");
            return m;
        });

        DailyMissionResponse expectedResponse = new DailyMissionResponse(
                "dm-accuracy-null", "Perfect Accuracy Default", MILESTONE, ACHIEVE_ACCURACY,
                null, 0, 1, 100, LocalDate.now(), LocalDate.now().plusDays(1), true
        );
        when(mapper.toDailyMissionResponse(any(DailyMission.class))).thenReturn(expectedResponse);

        DailyMissionResponse response = dailyMissionService.create(nullAccRequest);
        assertNotNull(response, "Created mission response should not be null");
    }

    @Test
    void create_WhenCountBasedWithNullTargetCount_ShouldUseDefaults() {
        DailyMissionRequest nullCountRequest = new DailyMissionRequest();
        nullCountRequest.setName("Count Default");
        nullCountRequest.setMilestone(MILESTONE);
        nullCountRequest.setMissionType(READ_N_ARTICLES);
        nullCountRequest.setRewardScore(100);

        doNothing().when(validator).validateDailyMissionRequest(nullCountRequest);
        when(dailyMissionRepository.findByNameIgnoreCase(nullCountRequest.getName())).thenReturn(Optional.empty());
        when(dailyMissionRepository.save(any(DailyMission.class))).thenAnswer(invocation -> {
            CountBasedDailyMission m = invocation.getArgument(0);
            assertAll("Verify default parameters for count mission",
                    () -> assertEquals(1, m.getTargetCount(), "Target count should default to 1"),
                    () -> assertEquals(LocalDate.now(), m.getActiveFrom(), "ActiveFrom should default to today"),
                    () -> assertEquals(LocalDate.now().plusDays(1), m.getActiveUntil(), "ActiveUntil should default to tomorrow"));
            m.setId("dm-count-null");
            return m;
        });

        DailyMissionResponse expectedResponse = new DailyMissionResponse(
                "dm-count-null", "Count Default", MILESTONE, READ_N_ARTICLES,
                1, null, null, 100, LocalDate.now(), LocalDate.now().plusDays(1), true
        );
        when(mapper.toDailyMissionResponse(any(DailyMission.class))).thenReturn(expectedResponse);

        DailyMissionResponse response = dailyMissionService.create(nullCountRequest);
        assertNotNull(response, "Created mission response should not be null");
    }

    @Test
    void create_WhenDuplicateName_ShouldThrowException() {
        doNothing().when(validator).validateDailyMissionRequest(countRequest);
        CountBasedDailyMission existing = new CountBasedDailyMission();
        existing.setName(countRequest.getName());
        when(dailyMissionRepository.findByNameIgnoreCase(countRequest.getName())).thenReturn(Optional.of(existing));

        assertAll("Verify exception when creating duplicate mission name",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> dailyMissionService.create(countRequest),
                            SHOULD_THROW);
                    assertEquals("DUPLICATE_NAME", ex.getErrorCode(), "ErrorCode should be DUPLICATE_NAME");
                });
    }

    @Test
    void update_WhenValid_ShouldSaveAndReturnResponse() {
        doNothing().when(validator).validateDailyMissionRequest(countRequest);
        doNothing().when(validator).validateMasterId(DM_123);

        CountBasedDailyMission existing = new CountBasedDailyMission();
        existing.setId(DM_123);
        existing.setName(OLD_NAME);
        existing.setMissionType(READ_N_ARTICLES);

        when(dailyMissionRepository.findById(DM_123)).thenReturn(Optional.of(existing));
        when(dailyMissionRepository.findByNameIgnoreCase(countRequest.getName())).thenReturn(Optional.empty());
        when(dailyMissionRepository.save(any(DailyMission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DailyMissionResponse expectedResponse = new DailyMissionResponse(
                DM_123, "Read 3 Articles", "Read 3 articles today", READ_N_ARTICLES,
                3, null, null, 50, LocalDate.now(), LocalDate.now().plusDays(1), true
        );
        when(mapper.toDailyMissionResponse(any(DailyMission.class))).thenReturn(expectedResponse);

        DailyMissionResponse response = dailyMissionService.update(DM_123, countRequest);

        assertAll("Verify updated daily mission response",
                () -> assertNotNull(response, "Response should not be null"),
                () -> assertEquals(countRequest.getName(), response.name(), "Name should match updated request"));
    }

    @Test
    void update_WhenAccuracyBasedWithNullThresholdAndCount_ShouldUseDefaults() {
        DailyMissionRequest nullAccRequest = new DailyMissionRequest();
        nullAccRequest.setName("Perfect Accuracy Default Updated");
        nullAccRequest.setMilestone(MILESTONE);
        nullAccRequest.setMissionType(ACHIEVE_ACCURACY);
        nullAccRequest.setRewardScore(100);

        doNothing().when(validator).validateDailyMissionRequest(nullAccRequest);
        doNothing().when(validator).validateMasterId(DM_123);

        AccuracyDailyMission existing = new AccuracyDailyMission();
        existing.setId(DM_123);
        existing.setName(OLD_NAME);
        existing.setMissionType(ACHIEVE_ACCURACY);

        when(dailyMissionRepository.findById(DM_123)).thenReturn(Optional.of(existing));
        when(dailyMissionRepository.findByNameIgnoreCase(nullAccRequest.getName())).thenReturn(Optional.empty());
        when(dailyMissionRepository.save(any(DailyMission.class))).thenAnswer(invocation -> {
            AccuracyDailyMission m = invocation.getArgument(0);
            assertAll("Verify default accuracy values on update",
                    () -> assertEquals(0, m.getAccuracyThreshold(), "Accuracy threshold should default to 0"),
                    () -> assertEquals(1, m.getRequiredCount(), "Required count should default to 1"));
            return m;
        });

        DailyMissionResponse expectedResponse = new DailyMissionResponse(
                DM_123, "Perfect Accuracy Default Updated", MILESTONE, ACHIEVE_ACCURACY,
                null, 0, 1, 100, LocalDate.now(), LocalDate.now().plusDays(1), true
        );
        when(mapper.toDailyMissionResponse(any(DailyMission.class))).thenReturn(expectedResponse);

        DailyMissionResponse response = dailyMissionService.update(DM_123, nullAccRequest);
        assertNotNull(response, "Updated mission response should not be null");
    }

    @Test
    void update_WhenCountBasedWithNullTargetCount_ShouldUseDefaults() {
        DailyMissionRequest nullCountRequest = new DailyMissionRequest();
        nullCountRequest.setName("Count Default Updated");
        nullCountRequest.setMilestone(MILESTONE);
        nullCountRequest.setMissionType(READ_N_ARTICLES);
        nullCountRequest.setRewardScore(100);

        doNothing().when(validator).validateDailyMissionRequest(nullCountRequest);
        doNothing().when(validator).validateMasterId(DM_123);

        CountBasedDailyMission existing = new CountBasedDailyMission();
        existing.setId(DM_123);
        existing.setName(OLD_NAME);
        existing.setMissionType(READ_N_ARTICLES);

        when(dailyMissionRepository.findById(DM_123)).thenReturn(Optional.of(existing));
        when(dailyMissionRepository.findByNameIgnoreCase(nullCountRequest.getName())).thenReturn(Optional.empty());
        when(dailyMissionRepository.save(any(DailyMission.class))).thenAnswer(invocation -> {
            CountBasedDailyMission m = invocation.getArgument(0);
            assertEquals(1, m.getTargetCount(), "Target count should default to 1");
            return m;
        });

        DailyMissionResponse expectedResponse = new DailyMissionResponse(
                DM_123, "Count Default Updated", MILESTONE, READ_N_ARTICLES,
                1, null, null, 100, LocalDate.now(), LocalDate.now().plusDays(1), true
        );
        when(mapper.toDailyMissionResponse(any(DailyMission.class))).thenReturn(expectedResponse);

        DailyMissionResponse response = dailyMissionService.update(DM_123, nullCountRequest);
        assertNotNull(response, "Updated mission response should not be null");
    }

    @Test
    void update_WhenTypeChanged_ShouldThrowException() {
        doNothing().when(validator).validateDailyMissionRequest(accuracyRequest);
        doNothing().when(validator).validateMasterId(DM_123);

        CountBasedDailyMission existing = new CountBasedDailyMission();
        existing.setId(DM_123);
        existing.setName(OLD_NAME);
        existing.setMissionType(READ_N_ARTICLES);

        when(dailyMissionRepository.findById(DM_123)).thenReturn(Optional.of(existing));
        when(dailyMissionRepository.findByNameIgnoreCase(accuracyRequest.getName())).thenReturn(Optional.empty());

        assertAll("Verify exception when changing daily mission type",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> dailyMissionService.update(DM_123, accuracyRequest),
                            SHOULD_THROW);
                    assertEquals("INVALID_TYPE_CHANGE", ex.getErrorCode(), "ErrorCode should be INVALID_TYPE_CHANGE");
                });
    }

    @Test
    void update_WhenNotFound_ShouldThrowException() {
        doNothing().when(validator).validateDailyMissionRequest(countRequest);
        doNothing().when(validator).validateMasterId(DM_123);
        when(dailyMissionRepository.findById(DM_123)).thenReturn(Optional.empty());

        assertAll("Verify exception when daily mission to update not found",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> dailyMissionService.update(DM_123, countRequest),
                            SHOULD_THROW);
                    assertEquals("NOT_FOUND", ex.getErrorCode(), "ErrorCode should be NOT_FOUND");
                });
    }

    @Test
    void update_WhenDuplicateNameForAnother_ShouldThrowException() {
        doNothing().when(validator).validateDailyMissionRequest(countRequest);
        doNothing().when(validator).validateMasterId(DM_123);

        CountBasedDailyMission existing = new CountBasedDailyMission();
        existing.setId(DM_123);
        existing.setName(OLD_NAME);
        existing.setMissionType(READ_N_ARTICLES);

        CountBasedDailyMission other = new CountBasedDailyMission();
        other.setId("dm-other");
        other.setName(countRequest.getName());

        when(dailyMissionRepository.findById(DM_123)).thenReturn(Optional.of(existing));
        when(dailyMissionRepository.findByNameIgnoreCase(countRequest.getName())).thenReturn(Optional.of(other));

        assertAll("Verify exception when updating to a duplicate name of another mission",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> dailyMissionService.update(DM_123, countRequest),
                            SHOULD_THROW);
                    assertEquals("DUPLICATE_NAME", ex.getErrorCode(), "ErrorCode should be DUPLICATE_NAME");
                });
    }

    @Test
    void delete_WhenExists_ShouldDelete() {
        doNothing().when(validator).validateMasterId(DM_123);
        CountBasedDailyMission existing = new CountBasedDailyMission();
        existing.setId(DM_123);
        when(dailyMissionRepository.findById(DM_123)).thenReturn(Optional.of(existing));

        dailyMissionService.delete(DM_123);

        verify(dailyMissionRepository).delete(existing);
    }

    @Test
    void delete_WhenNotFound_ShouldThrowException() {
        doNothing().when(validator).validateMasterId(DM_123);
        when(dailyMissionRepository.findById(DM_123)).thenReturn(Optional.empty());

        assertAll("Verify exception when deleting non-existent daily mission",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> dailyMissionService.delete(DM_123),
                            SHOULD_THROW);
                    assertEquals("NOT_FOUND", ex.getErrorCode(), "ErrorCode should be NOT_FOUND");
                });
    }

    @Test
    void findAll_ShouldReturnAll() {
        CountBasedDailyMission dm1 = new CountBasedDailyMission();
        dm1.setId(DM_1);
        AccuracyDailyMission dm2 = new AccuracyDailyMission();
        dm2.setId("dm-2");

        when(dailyMissionRepository.findAll()).thenReturn(List.of(dm1, dm2));

        List<DailyMissionResponse> responses = dailyMissionService.findAll();

        assertEquals(2, responses.size(), "Responses size should be 2");
    }
}
