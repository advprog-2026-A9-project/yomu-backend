package id.ac.ui.cs.advprog.yomu.gamification.validation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementRequest;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionRequest;
import id.ac.ui.cs.advprog.yomu.gamification.exception.GamificationException;
import id.ac.ui.cs.advprog.yomu.gamification.model.AchievementMilestoneType;
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMissionType;

class GamificationValidatorImplTest {

    // ── Shared test data ──────────────────────────────────────────────────────
    private static final String VALID_ACH_NAME = "Master Reader";
    private static final String VALID_MILESTONE = "Read 5 books with 90% accuracy";
    private static final String TYPE_ACHIEVEMENT_COUNT = AchievementMilestoneType.READINGS_COMPLETED.toString();
    private static final String TYPE_ACHIEVEMENT_ACCURACY = AchievementMilestoneType.ACCURACY_ABOVE.toString();
    private static final String TYPE_RANKING = AchievementMilestoneType.RANKING_ACHIEVED.toString();
    private static final String TYPE_MISSION_COUNT = DailyMissionType.READ_N_ARTICLES.toString();
    private static final String TYPE_MISSION_ACCURACY = DailyMissionType.ACHIEVE_ACCURACY.toString();
    private static final String VALID_MISSION_NAME = "Read 3 Articles";
    private static final String VALID_MISSION_STONE = "Read 3 articles today";
    private static final String VALID_USERNAME = "player-1";
    private static final String VALID_MASTER_ID = "mission-abc";

    // ── Assertion message constants ───────────────────────────────────────────
    private static final String MSG_THROWS = "Should throw GamificationException";
    private static final String MSG_MESSAGE = "Exception message should match";
    private static final String MSG_CODE = "Error code should match";
    private static final String MSG_NO_THROW = "Valid input should not throw";

    private GamificationValidatorImpl validator;
    private AchievementRequest validAchRequest;
    private DailyMissionRequest validMissionRequest;

    @BeforeEach
    void setUp() {
        validator = new GamificationValidatorImpl();

        validAchRequest = new AchievementRequest();
        validAchRequest.setName(VALID_ACH_NAME);
        validAchRequest.setMilestone(VALID_MILESTONE);
        validAchRequest.setMilestoneType(TYPE_ACHIEVEMENT_COUNT);
        validAchRequest.setMilestoneThreshold(5);

        validMissionRequest = new DailyMissionRequest();
        validMissionRequest.setName(VALID_MISSION_NAME);
        validMissionRequest.setMilestone(VALID_MISSION_STONE);
        validMissionRequest.setMissionType(TYPE_MISSION_COUNT);
        validMissionRequest.setTargetCount(3);
        validMissionRequest.setRewardScore(50);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // validateAchievementRequest
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    void validateAchievementRequest_WhenValid_ShouldNotThrow() {
        assertDoesNotThrow(() -> validator.validateAchievementRequest(validAchRequest), MSG_NO_THROW);
    }

    @Test
    void validateAchievementRequest_WhenNull_ShouldThrow() {
        assertAll("Verify null achievement request throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateAchievementRequest(null), MSG_THROWS);
                    assertEquals("Achievement request cannot be null", ex.getMessage(), MSG_MESSAGE);
                    assertEquals("INVALID_REQUEST", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateAchievementRequest_WhenNameEmpty_ShouldThrow() {
        validAchRequest.setName("");
        assertAll("Verify empty name throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateAchievementRequest(validAchRequest), MSG_THROWS);
                    assertEquals("INVALID_NAME", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateAchievementRequest_WhenNameBlank_ShouldThrow() {
        validAchRequest.setName("   ");
        assertAll("Verify blank name throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateAchievementRequest(validAchRequest), MSG_THROWS);
                    assertEquals("INVALID_NAME", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateAchievementRequest_WhenNameTooLong_ShouldThrow() {
        validAchRequest.setName("A".repeat(101));
        assertAll("Verify name over 100 chars throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateAchievementRequest(validAchRequest), MSG_THROWS);
                    assertEquals("NAME_TOO_LONG", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateAchievementRequest_WhenMilestoneEmpty_ShouldThrow() {
        validAchRequest.setMilestone("");
        assertAll("Verify empty milestone throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateAchievementRequest(validAchRequest), MSG_THROWS);
                    assertEquals("INVALID_MILESTONE", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateAchievementRequest_WhenMilestoneTooLong_ShouldThrow() {
        validAchRequest.setMilestone("M".repeat(256));
        assertAll("Verify milestone over 255 chars throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateAchievementRequest(validAchRequest), MSG_THROWS);
                    assertEquals("MILESTONE_TOO_LONG", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateAchievementRequest_WhenMilestoneTypeBlank_ShouldThrow() {
        validAchRequest.setMilestoneType("  ");
        assertAll("Verify blank milestone type throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateAchievementRequest(validAchRequest), MSG_THROWS);
                    assertEquals("INVALID_MILESTONE_TYPE", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateAchievementRequest_WhenMilestoneTypeUnknown_ShouldThrow() {
        validAchRequest.setMilestoneType("not_a_type");
        assertAll("Verify unknown milestone type throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateAchievementRequest(validAchRequest), MSG_THROWS);
                    assertEquals("INVALID_MILESTONE_TYPE", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateAchievementRequest_WhenThresholdZero_ShouldThrow() {
        validAchRequest.setMilestoneThreshold(0);
        assertAll("Verify zero threshold throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateAchievementRequest(validAchRequest), MSG_THROWS);
                    assertEquals("INVALID_MILESTONE_THRESHOLD", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateAchievementRequest_WhenRankingAndTargetTierNull_ShouldThrow() {
        validAchRequest.setMilestoneType(TYPE_RANKING);
        validAchRequest.setTargetTier(null);
        assertAll("Verify ranking type without target tier throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateAchievementRequest(validAchRequest), MSG_THROWS);
                    assertEquals("INVALID_TARGET_TIER", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateAchievementRequest_WhenRankingAndTargetTierInvalid_ShouldThrow() {
        validAchRequest.setMilestoneType(TYPE_RANKING);
        validAchRequest.setTargetTier("PLATINUM");
        assertAll("Verify invalid target tier throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateAchievementRequest(validAchRequest), MSG_THROWS);
                    assertEquals("INVALID_TARGET_TIER", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateAchievementRequest_WhenRankingAndValidTier_ShouldNotThrow() {
        validAchRequest.setMilestoneType(TYPE_RANKING);
        validAchRequest.setTargetTier("GOLD");
        assertDoesNotThrow(() -> validator.validateAchievementRequest(validAchRequest), MSG_NO_THROW);
    }

    @Test
    void validateAchievementRequest_WhenAccuracyType_ShouldNotThrow() {
        validAchRequest.setMilestoneType(TYPE_ACHIEVEMENT_ACCURACY);
        assertDoesNotThrow(() -> validator.validateAchievementRequest(validAchRequest), MSG_NO_THROW);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // validateDailyMissionRequest
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    void validateDailyMissionRequest_WhenValid_ShouldNotThrow() {
        assertDoesNotThrow(() -> validator.validateDailyMissionRequest(validMissionRequest), MSG_NO_THROW);
    }

    @Test
    void validateDailyMissionRequest_WhenNull_ShouldThrow() {
        assertAll("Verify null daily mission request throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(null), MSG_THROWS);
                    assertEquals("INVALID_REQUEST", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateDailyMissionRequest_WhenNameBlank_ShouldThrow() {
        validMissionRequest.setName("  ");
        assertAll("Verify blank mission name throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validMissionRequest), MSG_THROWS);
                    assertEquals("INVALID_NAME", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateDailyMissionRequest_WhenNameTooLong_ShouldThrow() {
        validMissionRequest.setName("X".repeat(101));
        assertAll("Verify mission name over 100 chars throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validMissionRequest), MSG_THROWS);
                    assertEquals("NAME_TOO_LONG", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateDailyMissionRequest_WhenMilestoneBlank_ShouldThrow() {
        validMissionRequest.setMilestone("  ");
        assertAll("Verify blank mission milestone throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validMissionRequest), MSG_THROWS);
                    assertEquals("INVALID_MILESTONE", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateDailyMissionRequest_WhenMilestoneTooLong_ShouldThrow() {
        validMissionRequest.setMilestone("M".repeat(256));
        assertAll("Verify mission milestone over 255 chars throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validMissionRequest), MSG_THROWS);
                    assertEquals("MILESTONE_TOO_LONG", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateDailyMissionRequest_WhenMissionTypeBlank_ShouldThrow() {
        validMissionRequest.setMissionType("  ");
        assertAll("Verify blank mission type throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validMissionRequest), MSG_THROWS);
                    assertEquals("INVALID_MISSION_TYPE", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateDailyMissionRequest_WhenMissionTypeInvalid_ShouldThrow() {
        validMissionRequest.setMissionType("do_nothing");
        assertAll("Verify unknown mission type throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validMissionRequest), MSG_THROWS);
                    assertEquals("INVALID_MISSION_TYPE", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateDailyMissionRequest_WhenCountBasedAndTargetCountNull_ShouldThrow() {
        validMissionRequest.setMissionType(TYPE_MISSION_COUNT);
        validMissionRequest.setTargetCount(null);
        assertAll("Verify count-based null target count throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validMissionRequest), MSG_THROWS);
                    assertEquals("INVALID_TARGET_COUNT", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateDailyMissionRequest_WhenCountBasedAndTargetCountZero_ShouldThrow() {
        validMissionRequest.setMissionType(TYPE_MISSION_COUNT);
        validMissionRequest.setTargetCount(0);
        assertAll("Verify count-based zero target count throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validMissionRequest), MSG_THROWS);
                    assertEquals("INVALID_TARGET_COUNT", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateDailyMissionRequest_WhenAccuracyAndThresholdZero_ShouldThrow() {
        validMissionRequest.setMissionType(TYPE_MISSION_ACCURACY);
        validMissionRequest.setAccuracyThreshold(0);
        validMissionRequest.setRequiredCount(1);
        assertAll("Verify accuracy type zero threshold throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validMissionRequest), MSG_THROWS);
                    assertEquals("INVALID_ACCURACY_THRESHOLD", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateDailyMissionRequest_WhenAccuracyAndThresholdAbove100_ShouldThrow() {
        validMissionRequest.setMissionType(TYPE_MISSION_ACCURACY);
        validMissionRequest.setAccuracyThreshold(101);
        validMissionRequest.setRequiredCount(1);
        assertAll("Verify accuracy threshold over 100 throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validMissionRequest), MSG_THROWS);
                    assertEquals("INVALID_ACCURACY_THRESHOLD", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateDailyMissionRequest_WhenAccuracyAndRequiredCountZero_ShouldThrow() {
        validMissionRequest.setMissionType(TYPE_MISSION_ACCURACY);
        validMissionRequest.setAccuracyThreshold(80);
        validMissionRequest.setRequiredCount(0);
        assertAll("Verify accuracy type zero required count throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validMissionRequest), MSG_THROWS);
                    assertEquals("INVALID_REQUIRED_COUNT", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateDailyMissionRequest_WhenAccuracyValid_ShouldNotThrow() {
        validMissionRequest.setMissionType(TYPE_MISSION_ACCURACY);
        validMissionRequest.setAccuracyThreshold(80);
        validMissionRequest.setRequiredCount(3);
        assertDoesNotThrow(() -> validator.validateDailyMissionRequest(validMissionRequest), MSG_NO_THROW);
    }

    @Test
    void validateDailyMissionRequest_WhenRewardScoreNull_ShouldThrow() {
        validMissionRequest.setRewardScore(null);
        assertAll("Verify null reward score throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validMissionRequest), MSG_THROWS);
                    assertEquals("INVALID_REWARD_SCORE", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateDailyMissionRequest_WhenRewardScoreZero_ShouldThrow() {
        validMissionRequest.setRewardScore(0);
        assertAll("Verify zero reward score throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validMissionRequest), MSG_THROWS);
                    assertEquals("INVALID_REWARD_SCORE", ex.getErrorCode(), MSG_CODE);
                });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // validateUsername
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    void validateUsername_WhenValid_ShouldNotThrow() {
        assertDoesNotThrow(() -> validator.validateUsername(VALID_USERNAME), MSG_NO_THROW);
    }

    @Test
    void validateUsername_WhenNull_ShouldThrow() {
        assertAll("Verify null username throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateUsername(null), MSG_THROWS);
                    assertEquals("INVALID_USERNAME", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateUsername_WhenBlank_ShouldThrow() {
        assertAll("Verify blank username throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateUsername("   "), MSG_THROWS);
                    assertEquals("INVALID_USERNAME", ex.getErrorCode(), MSG_CODE);
                });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // validateMasterId
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    void validateMasterId_WhenValid_ShouldNotThrow() {
        assertDoesNotThrow(() -> validator.validateMasterId(VALID_MASTER_ID), MSG_NO_THROW);
    }

    @Test
    void validateMasterId_WhenNull_ShouldThrow() {
        assertAll("Verify null master id throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateMasterId(null), MSG_THROWS);
                    assertEquals("INVALID_MASTER_ID", ex.getErrorCode(), MSG_CODE);
                });
    }

    @Test
    void validateMasterId_WhenBlank_ShouldThrow() {
        assertAll("Verify blank master id throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateMasterId("  "), MSG_THROWS);
                    assertEquals("INVALID_MASTER_ID", ex.getErrorCode(), MSG_CODE);
                });
    }
}
