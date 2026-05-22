package id.ac.ui.cs.advprog.yomu.gamification.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.gamification.exception.GamificationException;
import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedDailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.repository.DailyMissionRepository;
import id.ac.ui.cs.advprog.yomu.gamification.service.mission.DailyMissionRotationServiceImpl;
import id.ac.ui.cs.advprog.yomu.gamification.validation.GamificationValidator;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class DailyMissionRotationServiceImplTest {

    private static final String M_1 = "m-1";
    private static final String M_2 = "m-2";
    private static final String M_3 = "m-3";
    private static final String INVALID_REQUEST = "INVALID_REQUEST";

    @Mock
    private DailyMissionRepository dailyMissionRepository;

    @Mock
    private GamificationValidator validator;

    @InjectMocks
    private DailyMissionRotationServiceImpl rotationService;

    private LocalDate today;
    private List<DailyMission> activeMissions;
    private List<DailyMission> allMissions;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();

        CountBasedDailyMission m1 = new CountBasedDailyMission();
        m1.setId(M_1);
        m1.setActive(true);

        CountBasedDailyMission m2 = new CountBasedDailyMission();
        m2.setId(M_2);
        m2.setActive(true);

        CountBasedDailyMission m3 = new CountBasedDailyMission();
        m3.setId(M_3);
        m3.setActive(true);

        activeMissions = List.of(m1, m2, m3);

        CountBasedDailyMission m4 = new CountBasedDailyMission();
        m4.setId("m-4");
        m4.setActive(true);

        CountBasedDailyMission m5 = new CountBasedDailyMission();
        m5.setId("m-5");
        m5.setActive(true);

        allMissions = List.of(m1, m2, m3, m4, m5);
    }

    @Test
    void ensureMissionsRotated_WhenAlreadyExist_ShouldDoNothing() {
        when(dailyMissionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(today, today))
                .thenReturn(activeMissions);

        rotationService.ensureMissionsRotated(today);

        verify(dailyMissionRepository, never()).findAll();
    }

    @Test
    void ensureMissionsRotated_WhenEmpty_ShouldRotate() {
        when(dailyMissionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(today, today))
                .thenReturn(List.of());
        when(dailyMissionRepository.findAll()).thenReturn(allMissions);

        rotationService.ensureMissionsRotated(today);

        verify(dailyMissionRepository, times(3)).save(any(DailyMission.class));
    }

    @Test
    void rotateMissions_WhenAlreadyExist_ShouldDoNothing() {
        when(dailyMissionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(today, today))
                .thenReturn(activeMissions);

        rotationService.rotateMissions();

        verify(dailyMissionRepository, never()).findAll();
    }

    @Test
    void rotateMissions_WhenPoolEmpty_ShouldDoNothing() {
        when(dailyMissionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(today, today))
                .thenReturn(List.of());
        when(dailyMissionRepository.findAll()).thenReturn(List.of());

        rotationService.rotateMissions();

        verify(dailyMissionRepository, never()).save(any());
    }

    @Test
    void forceRotateMissions_ShouldExpireExistingAndRotate() {
        CountBasedDailyMission m6 = new CountBasedDailyMission();
        m6.setId("m-6");
        m6.setActive(true);

        List<DailyMission> rotationPool = List.of(
            allMissions.get(3), // m-4
            allMissions.get(4), // m-5
            m6
        );

        when(dailyMissionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(today, today))
                .thenReturn(activeMissions, List.of());
        when(dailyMissionRepository.findAll()).thenReturn(rotationPool);

        rotationService.forceRotateMissions();

        assertAll("Verify existing missions expired and rotation triggered",
                () -> assertEquals(today.minusDays(1), activeMissions.get(0).getActiveUntil(), "m-1 activeUntil should be set to yesterday"),
                () -> assertEquals(today.minusDays(1), activeMissions.get(1).getActiveUntil(), "m-2 activeUntil should be set to yesterday"),
                () -> assertEquals(today.minusDays(1), activeMissions.get(2).getActiveUntil(), "m-3 activeUntil should be set to yesterday"),
                () -> verify(dailyMissionRepository, times(6)).save(any(DailyMission.class)));
    }

    @Test
    void setTodayMissions_WhenValid_ShouldExpireAndSetMissions() {
        List<String> missionIds = List.of(M_1, M_2, M_3);
        when(dailyMissionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(today, today))
                .thenReturn(List.of());
        doNothing().when(validator).validateMasterId(anyString());
        when(dailyMissionRepository.findById(M_1)).thenReturn(Optional.of(allMissions.get(0)));
        when(dailyMissionRepository.findById(M_2)).thenReturn(Optional.of(allMissions.get(1)));
        when(dailyMissionRepository.findById(M_3)).thenReturn(Optional.of(allMissions.get(2)));

        rotationService.setTodayMissions(missionIds);

        verify(dailyMissionRepository, times(3)).save(any(DailyMission.class));
    }

    @Test
    void setTodayMissions_WhenSizeNotThree_ShouldThrowException() {
        assertAll("Verify exceptions when input size is invalid",
                () -> {
                    GamificationException exNull = assertThrows(GamificationException.class,
                            () -> rotationService.setTodayMissions(null),
                            "Should throw GamificationException for null input");
                    assertEquals(INVALID_REQUEST, exNull.getErrorCode(), "ErrorCode should be INVALID_REQUEST for null list");
                },
                () -> {
                    GamificationException exSize = assertThrows(GamificationException.class,
                            () -> rotationService.setTodayMissions(List.of(M_1, M_2)),
                            "Should throw GamificationException for list of size 2");
                    assertEquals(INVALID_REQUEST, exSize.getErrorCode(), "ErrorCode should be INVALID_REQUEST for size != 3");
                });
    }

    @Test
    void setTodayMissions_WhenMissionNotFound_ShouldThrowException() {
        List<String> missionIds = List.of(M_1, "m-invalid", M_3);
        when(dailyMissionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(today, today))
                .thenReturn(List.of());
        doNothing().when(validator).validateMasterId(anyString());
        when(dailyMissionRepository.findById(M_1)).thenReturn(Optional.of(allMissions.get(0)));
        when(dailyMissionRepository.findById("m-invalid")).thenReturn(Optional.empty());

        assertAll("Verify exception when target mission to set is not found",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> rotationService.setTodayMissions(missionIds),
                            "Should throw GamificationException for invalid mission ID");
                    assertEquals("NOT_FOUND", ex.getErrorCode(), "ErrorCode should be NOT_FOUND");
                });
    }

    @Test
    void getActiveDailyMissions_ShouldReturnActiveMissions() {
        when(dailyMissionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(today, today))
                .thenReturn(activeMissions);

        List<DailyMission> result = rotationService.getActiveDailyMissions(today);

        assertEquals(activeMissions, result, "Returned list of active missions should match activeMissions mock list");
    }
}
