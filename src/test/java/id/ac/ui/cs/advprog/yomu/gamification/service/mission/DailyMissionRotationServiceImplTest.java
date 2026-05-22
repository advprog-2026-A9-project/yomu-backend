package id.ac.ui.cs.advprog.yomu.gamification.service.mission;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.gamification.exception.GamificationException;
import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedDailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.repository.DailyMissionRepository;
import id.ac.ui.cs.advprog.yomu.gamification.validation.GamificationValidator;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class DailyMissionRotationServiceImplTest {

    // ── Shared data constants ─────────────────────────────────────────────────
    private static final String MISSION_A     = "mission-a";
    private static final String MISSION_B     = "mission-b";
    private static final String MISSION_C     = "mission-c";

    // ── Assertion message constants ───────────────────────────────────────────
    private static final String MSG_NO_THROW       = "Should not throw";
    private static final String MSG_SAVES          = "Repository save should be called for each selected mission";
    private static final String MSG_NO_SAVE        = "No save should happen when missions already active";
    private static final String MSG_THROW_INVALID  = "Should throw for invalid mission count";
    private static final String MSG_CODE_INVALID   = "Error code should be INVALID_REQUEST";

    @Mock private DailyMissionRepository missionRepository;
    @Mock private GamificationValidator validator;

    @InjectMocks
    private DailyMissionRotationServiceImpl rotationService;

    private CountBasedDailyMission missionA;
    private CountBasedDailyMission missionB;
    private CountBasedDailyMission missionC;

    @BeforeEach
    void setUp() {
        missionA = new CountBasedDailyMission();
        missionA.setTargetCount(3);
        missionA.setRewardScore(50);
        missionA.setName(MISSION_A);
        missionA.setMilestone("do a");
        missionA.setMissionType("count_based");

        missionB = new CountBasedDailyMission();
        missionB.setTargetCount(5);
        missionB.setRewardScore(100);
        missionB.setName(MISSION_B);
        missionB.setMilestone("do b");
        missionB.setMissionType("count_based");

        missionC = new CountBasedDailyMission();
        missionC.setTargetCount(1);
        missionC.setRewardScore(20);
        missionC.setName(MISSION_C);
        missionC.setMilestone("do c");
        missionC.setMissionType("count_based");
    }

    // ─── ensureMissionsRotated ────────────────────────────────────────────────

    @Test
    void ensureMissionsRotated_WhenMissionsAlreadyActive_ShouldSkipRotation() {
        when(missionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(any(), any()))
                .thenReturn(List.of(missionA));

        assertAll("Verify no save when missions are already active today",
                () -> assertDoesNotThrow(() -> rotationService.ensureMissionsRotated(LocalDate.now()), MSG_NO_THROW),
                () -> verify(missionRepository, never()).save(any()));
    }

    @Test
    void ensureMissionsRotated_WhenNoActiveMissions_ShouldRotate() {
        when(missionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(any(), any()))
                .thenReturn(List.of());
        when(missionRepository.findAll())
                .thenReturn(List.of(missionA, missionB, missionC));
        when(missionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertAll("Verify rotation saves missions when none active",
                () -> assertDoesNotThrow(() -> rotationService.ensureMissionsRotated(LocalDate.now()), MSG_NO_THROW),
                () -> verify(missionRepository, times(3)).save(any()));
    }

    @Test
    void ensureMissionsRotated_WhenPoolEmpty_ShouldDoNothing() {
        when(missionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(any(), any()))
                .thenReturn(List.of());
        when(missionRepository.findAll()).thenReturn(List.of());

        assertAll("Verify nothing happens when mission pool is empty",
                () -> assertDoesNotThrow(() -> rotationService.ensureMissionsRotated(LocalDate.now()), MSG_NO_THROW),
                () -> verify(missionRepository, never()).save(any()));
    }

    // ─── rotateMissions ──────────────────────────────────────────────────────

    @Test
    void rotateMissions_WhenPoolAvailable_ShouldSaveUpToThreeMissions() {
        when(missionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(any(), any()))
                .thenReturn(List.of());
        when(missionRepository.findAll())
                .thenReturn(List.of(missionA, missionB, missionC));
        when(missionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertAll("Verify rotateMissions saves 3 missions",
                () -> assertDoesNotThrow(() -> rotationService.rotateMissions(), MSG_NO_THROW),
                () -> verify(missionRepository, times(3)).save(any()));
    }

    @Test
    void rotateMissions_WhenAlreadyActive_ShouldSkip() {
        when(missionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(any(), any()))
                .thenReturn(List.of(missionA));

        assertAll("Verify rotateMissions skips when missions already active",
                () -> assertDoesNotThrow(() -> rotationService.rotateMissions(), MSG_NO_THROW),
                () -> verify(missionRepository, never()).save(any()));
    }

    // ─── forceRotateMissions ──────────────────────────────────────────────────

    @Test
    void forceRotateMissions_ShouldExpireAndThenRotate() {
        // First call (expire check) returns one active; second call (rotate check) returns empty
        when(missionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(any(), any()))
                .thenReturn(List.of(missionA))  // for expiry
                .thenReturn(List.of());         // for rotate
        when(missionRepository.findAll())
                .thenReturn(List.of(missionA, missionB, missionC));
        when(missionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertAll("Verify forceRotate expires old and saves new missions",
                () -> assertDoesNotThrow(() -> rotationService.forceRotateMissions(), MSG_NO_THROW),
                // 1 expire save + 3 new saves = 4
                () -> verify(missionRepository, times(4)).save(any()));
    }

    // ─── setTodayMissions ────────────────────────────────────────────────────

    @Test
    void setTodayMissions_WhenExactlyThreeIds_ShouldSaveAllThree() {
        List<String> ids = List.of(MISSION_A, MISSION_B, MISSION_C);
        when(missionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(any(), any()))
                .thenReturn(List.of());
        when(missionRepository.findById(MISSION_A)).thenReturn(Optional.of(missionA));
        when(missionRepository.findById(MISSION_B)).thenReturn(Optional.of(missionB));
        when(missionRepository.findById(MISSION_C)).thenReturn(Optional.of(missionC));
        when(missionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertAll("Verify setTodayMissions saves all 3 specified missions",
                () -> assertDoesNotThrow(() -> rotationService.setTodayMissions(ids), MSG_NO_THROW),
                () -> verify(missionRepository, times(3)).save(any()));
    }

    @Test
    void setTodayMissions_WhenNotThreeIds_ShouldThrow() {
        List<String> ids = List.of(MISSION_A, MISSION_B);
        assertAll("Verify setTodayMissions rejects count != 3",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> rotationService.setTodayMissions(ids), MSG_THROW_INVALID);
                    assertEquals("INVALID_REQUEST", ex.getErrorCode(), MSG_CODE_INVALID);
                });
    }

    @Test
    void setTodayMissions_WhenNull_ShouldThrow() {
        assertAll("Verify setTodayMissions rejects null list",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> rotationService.setTodayMissions(null), MSG_THROW_INVALID);
                    assertEquals("INVALID_REQUEST", ex.getErrorCode(), MSG_CODE_INVALID);
                });
    }

    @Test
    void setTodayMissions_WhenMissionIdNotFound_ShouldThrow() {
        List<String> ids = List.of(MISSION_A, MISSION_B, MISSION_C);
        when(missionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(any(), any()))
                .thenReturn(List.of());
        when(missionRepository.findById(MISSION_A)).thenReturn(Optional.empty());

        assertAll("Verify exception when a mission id is not found",
                () -> assertThrows(GamificationException.class,
                        () -> rotationService.setTodayMissions(ids),
                        "Should throw GamificationException when mission id not found"));
    }

    // ─── getActiveDailyMissions ──────────────────────────────────────────────

    @Test
    void getActiveDailyMissions_ShouldReturnList() {
        LocalDate date = LocalDate.now();
        when(missionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(date, date))
                .thenReturn(List.of(missionA, missionB));

        List<CountBasedDailyMission> result =
                (List<CountBasedDailyMission>) (List<?>) rotationService.getActiveDailyMissions(date);

        assertAll("Verify active missions list is returned correctly",
                () -> assertEquals(2, result.size(), "Should return 2 active missions"),
                () -> assertDoesNotThrow(() -> verify(missionRepository)
                        .findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(
                                eq(date), eq(date)),
                        "Repository should be queried with the given date"));
    }
}
