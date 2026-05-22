package id.ac.ui.cs.advprog.yomu.gamification.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class GamificationModelTest {

    // Assertion Messages
    private static final String MSG_TARGET_VALUE = "Target value should equal accuracy threshold";
    private static final String MSG_ELIGIBLE_TRUE = "Should be eligible for update";
    private static final String MSG_ELIGIBLE_FALSE = "Should not be eligible for update";
    private static final String MSG_PROGRESS_VALUE = "Progress value should match expected calculation";
    private static final String MSG_EVENT_ELIGIBLE = "Event eligibility should match expected boolean";
    private static final String MSG_ENUM_FROM = "Enum from string lookup should match expected value";
    private static final String MSG_ENUM_TO_STRING = "Enum toString should return underlying value string";

    // ─── AccuracyDailyMission Tests ──────────────────────────────────────────

    @Test
    void testAccuracyDailyMission_TargetValueAndEligibility() {
        AccuracyDailyMission mission = new AccuracyDailyMission();
        mission.setAccuracyThreshold(80);
        mission.setRequiredCount(3);

        assertAll("Accuracy Daily Mission Properties",
                () -> assertEquals(80, mission.getTargetValue(), MSG_TARGET_VALUE),
                () -> assertTrue(mission.isEligibleForUpdate(80), MSG_ELIGIBLE_TRUE),
                () -> assertTrue(mission.isEligibleForUpdate(90), MSG_ELIGIBLE_TRUE),
                () -> assertFalse(mission.isEligibleForUpdate(79), MSG_ELIGIBLE_FALSE));
    }

    @Test
    void testAccuracyDailyMission_CalculateNewProgressValue() {
        AccuracyDailyMission mission = new AccuracyDailyMission();
        mission.setAccuracyThreshold(90);
        mission.setRequiredCount(3); // step = 30

        assertAll("Progress Calculations with Valid requiredCount",
                // step = 30, currentCompleted = 0, nextCompleted = 1 -> 30
                () -> assertEquals(30, mission.calculateNewProgressValue(0), MSG_PROGRESS_VALUE),
                // step = 30, currentCompleted = 1, nextCompleted = 2 -> 60
                () -> assertEquals(60, mission.calculateNewProgressValue(30), MSG_PROGRESS_VALUE),
                // step = 30, currentCompleted = 2, nextCompleted = 3 (>= 3) -> 90
                () -> assertEquals(90, mission.calculateNewProgressValue(60), MSG_PROGRESS_VALUE));
    }

    @Test
    void testAccuracyDailyMission_CalculateNewProgressValue_ZeroRequiredCount() {
        AccuracyDailyMission mission = new AccuracyDailyMission();
        mission.setAccuracyThreshold(90);
        mission.setRequiredCount(0); // requiredCount = 0 -> step = 0, currentCompleted = 0, nextCompleted = 1 -> >= 0 -> 90

        assertEquals(90, mission.calculateNewProgressValue(0), MSG_PROGRESS_VALUE);
    }

    // ─── DailyMission Event Eligibility Tests ─────────────────────────────────

    @Test
    void testDailyMission_IsEligibleForEvent() {
        DailyMission quizMission = new CountBasedDailyMission();
        quizMission.setMissionType("complete_n_quizzes");

        DailyMission readMission = new CountBasedDailyMission();
        readMission.setMissionType("read_n_articles");

        assertAll("DailyMission Event Eligibility",
                () -> assertTrue(quizMission.isEligibleForEvent(DailyMission.EventType.QUIZ_COMPLETED), MSG_EVENT_ELIGIBLE),
                () -> assertFalse(quizMission.isEligibleForEvent(DailyMission.EventType.READING_COMPLETED), MSG_EVENT_ELIGIBLE),
                () -> assertFalse(readMission.isEligibleForEvent(DailyMission.EventType.QUIZ_COMPLETED), MSG_EVENT_ELIGIBLE),
                () -> assertTrue(readMission.isEligibleForEvent(DailyMission.EventType.READING_COMPLETED), MSG_EVENT_ELIGIBLE),
                () -> assertFalse(quizMission.isEligibleForEvent(null), MSG_EVENT_ELIGIBLE));
    }

    // ─── AchievementMilestoneType Tests ───────────────────────────────────────

    @Test
    void testAchievementMilestoneType() {
        assertAll("AchievementMilestoneType enum mapping",
                () -> assertEquals(AchievementMilestoneType.READINGS_COMPLETED, AchievementMilestoneType.from("readings_completed"), MSG_ENUM_FROM),
                () -> assertEquals(AchievementMilestoneType.QUIZZES_PASSED, AchievementMilestoneType.from("  QUIZZES_PASSED  "), MSG_ENUM_FROM),
                () -> assertNull(AchievementMilestoneType.from("invalid_milestone"), MSG_ENUM_FROM),
                () -> assertNull(AchievementMilestoneType.from(null), MSG_ENUM_FROM),
                () -> assertEquals("accuracy_above", AchievementMilestoneType.ACCURACY_ABOVE.toString(), MSG_ENUM_TO_STRING));
    }

    // ─── DailyMissionType Tests ───────────────────────────────────────────────

    @Test
    void testDailyMissionType() {
        assertAll("DailyMissionType enum mapping",
                () -> assertEquals(DailyMissionType.READ_N_ARTICLES, DailyMissionType.from("read_n_articles"), MSG_ENUM_FROM),
                () -> assertEquals(DailyMissionType.COMPLETE_N_QUIZZES, DailyMissionType.from("  COMPLETE_N_QUIZZES  "), MSG_ENUM_FROM),
                () -> assertNull(DailyMissionType.from("invalid_type"), MSG_ENUM_FROM),
                () -> assertNull(DailyMissionType.from(null), MSG_ENUM_FROM),
                () -> assertEquals("achieve_accuracy", DailyMissionType.ACHIEVE_ACCURACY.toString(), MSG_ENUM_TO_STRING));
    }
}
