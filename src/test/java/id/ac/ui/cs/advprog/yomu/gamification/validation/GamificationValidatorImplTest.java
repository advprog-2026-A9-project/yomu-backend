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

class GamificationValidatorImplTest {

    private static final String ACHIEVE_ACCURACY = "achieve_accuracy";

    private GamificationValidatorImpl validator;
    private AchievementRequest validAchievementRequest;
    private DailyMissionRequest validDailyMissionRequest;

    @BeforeEach
    void setUp() {
        validator = new GamificationValidatorImpl();

        validAchievementRequest = new AchievementRequest();
        validAchievementRequest.setName("Master Reader");
        validAchievementRequest.setMilestone("Read 5 books with 90% accuracy");
        validAchievementRequest.setMilestoneType("accuracy_above");
        validAchievementRequest.setMilestoneThreshold(5);
        validAchievementRequest.setAccuracyThreshold(90);
        validAchievementRequest.setTier("Gold");

        validDailyMissionRequest = new DailyMissionRequest();
        validDailyMissionRequest.setName("Read 3 Articles");
        validDailyMissionRequest.setMilestone("Read 3 articles today");
        validDailyMissionRequest.setMissionType("read_n_articles");
        validDailyMissionRequest.setTargetCount(3);
        validDailyMissionRequest.setRewardScore(50);
    }

    @Test
    void validateAchievementRequest_WhenValid_ShouldPass() {
        assertDoesNotThrow(() -> validator.validateAchievementRequest(validAchievementRequest),
                "Should not throw exception when achievement request is valid");
    }

    @Test
    void validateAchievementRequest_WhenNull_ShouldThrowException() {
        assertAll("Verify achievement null request exception",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateAchievementRequest(null),
                            "Should throw GamificationException when request is null");
                    assertEquals("Achievement request cannot be null", ex.getMessage(), "Message should match");
                    assertEquals("INVALID_REQUEST", ex.getErrorCode(), "ErrorCode should be INVALID_REQUEST");
                });
    }

    @Test
    void validateAchievementRequest_WhenNameInvalid_ShouldThrowException() {
        assertAll("Verify invalid name exceptions",
                () -> {
                    validAchievementRequest.setName("");
                    GamificationException exEmpty = assertThrows(GamificationException.class,
                            () -> validator.validateAchievementRequest(validAchievementRequest),
                            "Should throw GamificationException when name is empty");
                    assertEquals("INVALID_NAME", exEmpty.getErrorCode(), "ErrorCode should be INVALID_NAME");
                },
                () -> {
                    validAchievementRequest.setName("a".repeat(101));
                    GamificationException exLong = assertThrows(GamificationException.class,
                            () -> validator.validateAchievementRequest(validAchievementRequest),
                            "Should throw GamificationException when name is too long");
                    assertEquals("NAME_TOO_LONG", exLong.getErrorCode(), "ErrorCode should be NAME_TOO_LONG");
                });
    }

    @Test
    void validateAchievementRequest_WhenMilestoneInvalid_ShouldThrowException() {
        assertAll("Verify invalid milestone exceptions",
                () -> {
                    validAchievementRequest.setMilestone(" ");
                    GamificationException exEmpty = assertThrows(GamificationException.class,
                            () -> validator.validateAchievementRequest(validAchievementRequest),
                            "Should throw GamificationException when milestone is blank");
                    assertEquals("INVALID_MILESTONE", exEmpty.getErrorCode(), "ErrorCode should be INVALID_MILESTONE");
                },
                () -> {
                    validAchievementRequest.setMilestone("a".repeat(256));
                    GamificationException exLong = assertThrows(GamificationException.class,
                            () -> validator.validateAchievementRequest(validAchievementRequest),
                            "Should throw GamificationException when milestone is too long");
                    assertEquals("MILESTONE_TOO_LONG", exLong.getErrorCode(), "ErrorCode should be MILESTONE_TOO_LONG");
                });
    }

    @Test
    void validateAchievementRequest_WhenMilestoneTypeInvalid_ShouldThrowException() {
        assertAll("Verify invalid milestone type exceptions",
                () -> {
                    validAchievementRequest.setMilestoneType(null);
                    GamificationException exNull = assertThrows(GamificationException.class,
                            () -> validator.validateAchievementRequest(validAchievementRequest),
                            "Should throw GamificationException when milestoneType is null");
                    assertEquals("INVALID_MILESTONE_TYPE", exNull.getErrorCode(), "ErrorCode should be INVALID_MILESTONE_TYPE for null");
                },
                () -> {
                    validAchievementRequest.setMilestoneType("invalid_type");
                    GamificationException exInvalid = assertThrows(GamificationException.class,
                            () -> validator.validateAchievementRequest(validAchievementRequest),
                            "Should throw GamificationException when milestoneType is invalid");
                    assertEquals("INVALID_MILESTONE_TYPE", exInvalid.getErrorCode(), "ErrorCode should be INVALID_MILESTONE_TYPE for invalid string");
                });
    }

    @Test
    void validateAchievementRequest_WhenThresholdInvalid_ShouldThrowException() {
        assertAll("Verify threshold validation",
                () -> {
                    validAchievementRequest.setMilestoneThreshold(0);
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateAchievementRequest(validAchievementRequest),
                            "Should throw GamificationException when milestoneThreshold is less than or equal to 0");
                    assertEquals("INVALID_MILESTONE_THRESHOLD", ex.getErrorCode(), "ErrorCode should be INVALID_MILESTONE_THRESHOLD");
                });
    }

    @Test
    void validateAchievementRequest_WhenRankingTypeAndTargetTierInvalid_ShouldThrowException() {
        assertAll("Verify ranking and target tier validation behavior",
                () -> {
                    validAchievementRequest.setMilestoneType("ranking_achieved");
                    validAchievementRequest.setTargetTier(null);
                    GamificationException exNull = assertThrows(GamificationException.class,
                            () -> validator.validateAchievementRequest(validAchievementRequest),
                            "Should throw GamificationException when targetTier is null");
                    assertEquals("INVALID_TARGET_TIER", exNull.getErrorCode(), "ErrorCode should be INVALID_TARGET_TIER for null");
                },
                () -> {
                    validAchievementRequest.setMilestoneType("ranking_achieved");
                    validAchievementRequest.setTargetTier("GODLIKE_BUT_WRONG");
                    GamificationException exInvalid = assertThrows(GamificationException.class,
                            () -> validator.validateAchievementRequest(validAchievementRequest),
                            "Should throw GamificationException when targetTier is invalid");
                    assertEquals("INVALID_TARGET_TIER", exInvalid.getErrorCode(), "ErrorCode should be INVALID_TARGET_TIER for invalid value");
                },
                () -> {
                    validAchievementRequest.setMilestoneType("ranking_achieved");
                    validAchievementRequest.setTargetTier("DIAMOND");
                    assertDoesNotThrow(() -> validator.validateAchievementRequest(validAchievementRequest),
                            "Should not throw exception when valid target tier is provided");
                });
    }

    @Test
    void validateDailyMissionRequest_WhenValid_ShouldPass() {
        assertDoesNotThrow(() -> validator.validateDailyMissionRequest(validDailyMissionRequest),
                "Should not throw exception when daily mission request is valid");
    }

    @Test
    void validateDailyMissionRequest_WhenNull_ShouldThrowException() {
        assertAll("Verify null daily mission request",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(null),
                            "Should throw GamificationException when daily mission request is null");
                    assertEquals("INVALID_REQUEST", ex.getErrorCode(), "ErrorCode should be INVALID_REQUEST");
                });
    }

    @Test
    void validateDailyMissionRequest_WhenNameInvalid_ShouldThrowException() {
        assertAll("Verify invalid name exceptions for daily mission request",
                () -> {
                    validDailyMissionRequest.setName(" ");
                    GamificationException exEmpty = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validDailyMissionRequest),
                            "Should throw GamificationException when name is blank");
                    assertEquals("INVALID_NAME", exEmpty.getErrorCode(), "ErrorCode should be INVALID_NAME");
                },
                () -> {
                    validDailyMissionRequest.setName("a".repeat(101));
                    GamificationException exLong = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validDailyMissionRequest),
                            "Should throw GamificationException when name is too long");
                    assertEquals("NAME_TOO_LONG", exLong.getErrorCode(), "ErrorCode should be NAME_TOO_LONG");
                });
    }

    @Test
    void validateDailyMissionRequest_WhenMilestoneInvalid_ShouldThrowException() {
        assertAll("Verify invalid milestone exceptions for daily mission request",
                () -> {
                    validDailyMissionRequest.setMilestone(null);
                    GamificationException exEmpty = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validDailyMissionRequest),
                            "Should throw GamificationException when milestone is null");
                    assertEquals("INVALID_MILESTONE", exEmpty.getErrorCode(), "ErrorCode should be INVALID_MILESTONE");
                },
                () -> {
                    validDailyMissionRequest.setMilestone("a".repeat(256));
                    GamificationException exLong = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validDailyMissionRequest),
                            "Should throw GamificationException when milestone is too long");
                    assertEquals("MILESTONE_TOO_LONG", exLong.getErrorCode(), "ErrorCode should be MILESTONE_TOO_LONG");
                });
    }

    @Test
    void validateDailyMissionRequest_WhenMissionTypeInvalid_ShouldThrowException() {
        assertAll("Verify invalid mission type exceptions",
                () -> {
                    validDailyMissionRequest.setMissionType(null);
                    GamificationException exNull = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validDailyMissionRequest),
                            "Should throw GamificationException when missionType is null");
                    assertEquals("INVALID_MISSION_TYPE", exNull.getErrorCode(), "ErrorCode should be INVALID_MISSION_TYPE for null");
                },
                () -> {
                    validDailyMissionRequest.setMissionType("invalid_type");
                    GamificationException exInvalid = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validDailyMissionRequest),
                            "Should throw GamificationException when missionType is invalid");
                    assertEquals("INVALID_MISSION_TYPE", exInvalid.getErrorCode(), "ErrorCode should be INVALID_MISSION_TYPE for invalid value");
                });
    }

    @Test
    void validateDailyMissionRequest_WhenAchieveAccuracyThresholdInvalid_ShouldThrowException() {
        assertAll("Verify invalid accuracy threshold exceptions",
                () -> {
                    validDailyMissionRequest.setMissionType(ACHIEVE_ACCURACY);
                    validDailyMissionRequest.setRequiredCount(1);
                    validDailyMissionRequest.setAccuracyThreshold(null);
                    GamificationException exNull = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validDailyMissionRequest),
                            "Should throw GamificationException when accuracyThreshold is null");
                    assertEquals("INVALID_ACCURACY_THRESHOLD", exNull.getErrorCode(), "ErrorCode should be INVALID_ACCURACY_THRESHOLD for null");
                },
                () -> {
                    validDailyMissionRequest.setMissionType(ACHIEVE_ACCURACY);
                    validDailyMissionRequest.setRequiredCount(1);
                    validDailyMissionRequest.setAccuracyThreshold(0);
                    GamificationException exZero = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validDailyMissionRequest),
                            "Should throw GamificationException when accuracyThreshold is 0");
                    assertEquals("INVALID_ACCURACY_THRESHOLD", exZero.getErrorCode(), "ErrorCode should be INVALID_ACCURACY_THRESHOLD for 0");
                },
                () -> {
                    validDailyMissionRequest.setMissionType(ACHIEVE_ACCURACY);
                    validDailyMissionRequest.setRequiredCount(1);
                    validDailyMissionRequest.setAccuracyThreshold(101);
                    GamificationException exTooHigh = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validDailyMissionRequest),
                            "Should throw GamificationException when accuracyThreshold is above 100");
                    assertEquals("INVALID_ACCURACY_THRESHOLD", exTooHigh.getErrorCode(), "ErrorCode should be INVALID_ACCURACY_THRESHOLD for >100");
                });
    }

    @Test
    void validateDailyMissionRequest_WhenAchieveAccuracyRequiredCountInvalid_ShouldThrowException() {
        assertAll("Verify invalid required count exceptions",
                () -> {
                    validDailyMissionRequest.setMissionType(ACHIEVE_ACCURACY);
                    validDailyMissionRequest.setAccuracyThreshold(90);
                    validDailyMissionRequest.setRequiredCount(null);
                    GamificationException exNull = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validDailyMissionRequest),
                            "Should throw GamificationException when requiredCount is null");
                    assertEquals("INVALID_REQUIRED_COUNT", exNull.getErrorCode(), "ErrorCode should be INVALID_REQUIRED_COUNT for null");
                },
                () -> {
                    validDailyMissionRequest.setMissionType(ACHIEVE_ACCURACY);
                    validDailyMissionRequest.setAccuracyThreshold(90);
                    validDailyMissionRequest.setRequiredCount(0);
                    GamificationException exZero = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validDailyMissionRequest),
                            "Should throw GamificationException when requiredCount is 0");
                    assertEquals("INVALID_REQUIRED_COUNT", exZero.getErrorCode(), "ErrorCode should be INVALID_REQUIRED_COUNT for 0");
                });
    }

    @Test
    void validateDailyMissionRequest_WhenCountBasedTargetCountInvalid_ShouldThrowException() {
        assertAll("Verify invalid target count exceptions",
                () -> {
                    validDailyMissionRequest.setMissionType("read_n_articles");
                    validDailyMissionRequest.setTargetCount(null);
                    GamificationException exNull = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validDailyMissionRequest),
                            "Should throw GamificationException when targetCount is null");
                    assertEquals("INVALID_TARGET_COUNT", exNull.getErrorCode(), "ErrorCode should be INVALID_TARGET_COUNT for null");
                },
                () -> {
                    validDailyMissionRequest.setMissionType("read_n_articles");
                    validDailyMissionRequest.setTargetCount(0);
                    GamificationException exZero = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validDailyMissionRequest),
                            "Should throw GamificationException when targetCount is 0");
                    assertEquals("INVALID_TARGET_COUNT", exZero.getErrorCode(), "ErrorCode should be INVALID_TARGET_COUNT for 0");
                });
    }

    @Test
    void validateDailyMissionRequest_WhenRewardScoreInvalid_ShouldThrowException() {
        assertAll("Verify invalid reward score exceptions",
                () -> {
                    validDailyMissionRequest.setRewardScore(null);
                    GamificationException exNull = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validDailyMissionRequest),
                            "Should throw GamificationException when rewardScore is null");
                    assertEquals("INVALID_REWARD_SCORE", exNull.getErrorCode(), "ErrorCode should be INVALID_REWARD_SCORE for null");
                },
                () -> {
                    validDailyMissionRequest.setRewardScore(0);
                    GamificationException exZero = assertThrows(GamificationException.class,
                            () -> validator.validateDailyMissionRequest(validDailyMissionRequest),
                            "Should throw GamificationException when rewardScore is 0");
                    assertEquals("INVALID_REWARD_SCORE", exZero.getErrorCode(), "ErrorCode should be INVALID_REWARD_SCORE for 0");
                });
    }

    @Test
    void validateUsername_ShouldCheckEmpty() {
        assertAll("Verify validateUsername behavior",
                () -> assertDoesNotThrow(() -> validator.validateUsername("user-1"), "Should pass for valid username"),
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateUsername(null),
                            "Should throw GamificationException when username is null");
                    assertEquals("INVALID_USERNAME", ex.getErrorCode(), "ErrorCode should be INVALID_USERNAME");
                });
    }

    @Test
    void validateMasterId_ShouldCheckEmpty() {
        assertAll("Verify validateMasterId behavior",
                () -> assertDoesNotThrow(() -> validator.validateMasterId("master-1"), "Should pass for valid masterId"),
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> validator.validateMasterId(" "),
                            "Should throw GamificationException when masterId is blank");
                    assertEquals("INVALID_MASTER_ID", ex.getErrorCode(), "ErrorCode should be INVALID_MASTER_ID");
                });
    }
}
