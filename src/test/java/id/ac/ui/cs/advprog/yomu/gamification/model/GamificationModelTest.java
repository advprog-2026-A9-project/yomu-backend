package id.ac.ui.cs.advprog.yomu.gamification.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class GamificationModelTest {

    private static final String MILESTONE = "Milestone";
    private static final String USER_1 = "user-1";

    @Test
    void testAchievementModels() {
        AccuracyBasedAchievement acc = new AccuracyBasedAchievement();
        acc.setId("ach-1");
        acc.setName("Acc Ach");
        acc.setMilestone(MILESTONE);
        acc.setMilestoneType("accuracy_above");
        acc.setMilestoneThreshold(10);
        acc.setTier("Gold");
        acc.setActive(true);
        acc.setAccuracyThreshold(90);
        LocalDateTime now = LocalDateTime.now();
        acc.setCreatedAt(now);
        acc.setUpdatedAt(now);

        RankingBasedAchievement rank = new RankingBasedAchievement();
        rank.setTargetTier("DIAMOND");

        CountBasedAchievement count = new CountBasedAchievement();

        assertAll("Verify achievement models properties",
                () -> assertEquals("ach-1", acc.getId(), "ID should match ach-1"),
                () -> assertEquals("Acc Ach", acc.getName(), "Name should match Acc Ach"),
                () -> assertEquals(MILESTONE, acc.getMilestone(), "Milestone should match MILESTONE"),
                () -> assertEquals("accuracy_above", acc.getMilestoneType(), "Milestone type should match accuracy_above"),
                () -> assertEquals(10, acc.getMilestoneThreshold(), "Milestone threshold should match 10"),
                () -> assertEquals("Gold", acc.getTier(), "Tier should match Gold"),
                () -> assertTrue(acc.isActive(), "Active flag should be true"),
                () -> assertEquals(90, acc.getAccuracyThreshold(), "Accuracy threshold should match 90"),
                () -> assertEquals(now, acc.getCreatedAt(), "CreatedAt timestamp should match now"),
                () -> assertEquals(now, acc.getUpdatedAt(), "UpdatedAt timestamp should match now"),
                () -> assertEquals("DIAMOND", rank.getTargetTier(), "Target tier should match DIAMOND"),
                () -> assertNotNull(count, "CountBasedAchievement instance should not be null")
        );
    }

    @Test
    void testDailyMissionModels() {
        CountBasedDailyMission countMission = new CountBasedDailyMission();
        countMission.setId("m-1");
        countMission.setName("Count Mission");
        countMission.setMilestone(MILESTONE);
        countMission.setMissionType("read_n_articles");
        countMission.setRewardScore(100);
        countMission.setActiveFrom(LocalDate.now());
        countMission.setActiveUntil(LocalDate.now());
        countMission.setActive(true);
        countMission.setTargetCount(5);

        AccuracyDailyMission accMission = new AccuracyDailyMission();
        accMission.setMissionType("achieve_accuracy");
        accMission.setAccuracyThreshold(80);
        accMission.setRequiredCount(2);

        assertAll("Verify daily mission models properties and calculations",
                () -> assertEquals("m-1", countMission.getId(), "Count mission ID should match m-1"),
                () -> assertEquals("Count Mission", countMission.getName(), "Count mission name should match Count Mission"),
                () -> assertEquals(MILESTONE, countMission.getMilestone(), "Count mission milestone should match MILESTONE"),
                () -> assertEquals("read_n_articles", countMission.getMissionType(), "Count mission type should match read_n_articles"),
                () -> assertEquals(100, countMission.getRewardScore(), "Count mission reward score should match 100"),
                () -> assertEquals(LocalDate.now(), countMission.getActiveFrom(), "Count mission activeFrom date should match today"),
                () -> assertEquals(LocalDate.now(), countMission.getActiveUntil(), "Count mission activeUntil date should match today"),
                () -> assertTrue(countMission.isActive(), "Count mission active flag should be true"),
                () -> assertEquals(5, countMission.getTargetCount(), "Count mission target count should match 5"),
                () -> assertEquals(5, countMission.getTargetValue(), "Count mission target value should match 5"),
                () -> assertTrue(countMission.isEligibleForUpdate(10), "Count mission should be eligible for update with value 10"),
                () -> assertEquals(4, countMission.calculateNewProgressValue(3), "New progress value should be current progress + step (3+1)"),
                () -> assertTrue(countMission.isEligibleForEvent(DailyMission.EventType.READING_COMPLETED), "Count mission should be eligible for READING_COMPLETED event"),
                () -> assertFalse(countMission.isEligibleForEvent(DailyMission.EventType.QUIZ_COMPLETED), "Count mission should not be eligible for QUIZ_COMPLETED event"),
                () -> assertEquals(80, accMission.getTargetValue(), "Accuracy mission target value should match accuracy threshold"),
                () -> assertTrue(accMission.isEligibleForUpdate(80), "Accuracy mission should be eligible for update at or above threshold"),
                () -> assertFalse(accMission.isEligibleForUpdate(79), "Accuracy mission should not be eligible for update below threshold"),
                () -> assertEquals(40, accMission.calculateNewProgressValue(0), "Progress calculation should step up properly from 0"),
                () -> assertEquals(80, accMission.calculateNewProgressValue(40), "Progress calculation should reach threshold when completed count is satisfied"),
                () -> {
                    accMission.setRequiredCount(0);
                    assertEquals(80, accMission.calculateNewProgressValue(10), "Progress calculation should handle requiredCount <= 0 properly");
                },
                () -> {
                    accMission.setRequiredCount(2);
                    accMission.setAccuracyThreshold(0);
                    assertEquals(0, accMission.calculateNewProgressValue(10), "Progress calculation should handle accuracyThreshold = 0 properly");
                },
                () -> {
                    accMission.setRequiredCount(2);
                    accMission.setAccuracyThreshold(80);
                    accMission.setMissionType("achieve_accuracy");
                    assertTrue(accMission.isEligibleForEvent(DailyMission.EventType.QUIZ_COMPLETED), "Accuracy mission should be eligible for QUIZ_COMPLETED event");
                    assertFalse(accMission.isEligibleForEvent(DailyMission.EventType.READING_COMPLETED), "Accuracy mission should not be eligible for READING_COMPLETED event");
                }
        );
    }

    @Test
    void testAchievementMilestoneType() {
        assertAll("Verify AchievementMilestoneType conversion and mapping",
                () -> assertEquals(AchievementMilestoneType.READINGS_COMPLETED, AchievementMilestoneType.from("readings_completed"), "Should convert readings_completed"),
                () -> assertEquals(AchievementMilestoneType.QUIZZES_PASSED, AchievementMilestoneType.from("  quizzes_passed  "), "Should trim and convert quizzes_passed"),
                () -> assertNull(AchievementMilestoneType.from(null), "Null should return null"),
                () -> assertNull(AchievementMilestoneType.from("invalid"), "Invalid type string should return null"),
                () -> assertEquals("accuracy_above", AchievementMilestoneType.ACCURACY_ABOVE.toString(), "toString should match accuracy_above"));
    }

    @Test
    void testDailyMissionType() {
        assertAll("Verify DailyMissionType conversion and mapping",
                () -> assertEquals(DailyMissionType.READ_N_ARTICLES, DailyMissionType.from("read_n_articles"), "Should convert read_n_articles"),
                () -> assertEquals(DailyMissionType.ACHIEVE_ACCURACY, DailyMissionType.from("  achieve_accuracy  "), "Should trim and convert achieve_accuracy"),
                () -> assertNull(DailyMissionType.from(null), "Null should return null"),
                () -> assertNull(DailyMissionType.from("invalid"), "Invalid type string should return null"),
                () -> assertEquals("complete_n_quizzes", DailyMissionType.COMPLETE_N_QUIZZES.toString(), "toString should match complete_n_quizzes"));
    }

    @Test
    void testUserAchievementProgress() {
        Achievement ach = new CountBasedAchievement();
        UserAchievementProgress progress = new UserAchievementProgress();
        progress.setId("p-1");
        progress.setUsername(USER_1);
        progress.setAchievement(ach);
        progress.setProgressValue(5);
        progress.setUnlocked(true);
        LocalDateTime now = LocalDateTime.now();
        progress.setUnlockedAt(now);
        progress.setCreatedAt(now);
        progress.setUpdatedAt(now);

        assertAll("Verify UserAchievementProgress properties",
                () -> assertEquals("p-1", progress.getId(), "ID should match p-1"),
                () -> assertEquals(USER_1, progress.getUsername(), "Username should match USER_1"),
                () -> assertEquals(ach, progress.getAchievement(), "Achievement reference should match"),
                () -> assertEquals(5, progress.getProgressValue(), "Progress value should match 5"),
                () -> assertTrue(progress.isUnlocked(), "Unlocked flag should be true"),
                () -> assertEquals(now, progress.getUnlockedAt(), "UnlockedAt timestamp should match now"),
                () -> assertEquals(now, progress.getCreatedAt(), "CreatedAt timestamp should match now"),
                () -> assertEquals(now, progress.getUpdatedAt(), "UpdatedAt timestamp should match now"));
    }

    @Test
    void testUserAchievementShowcase() {
        UserAchievementShowcase showcase = UserAchievementShowcase.builder()
                .username(USER_1)
                .achievementIds(List.of("ach-1", "ach-2"))
                .build();

        UserAchievementShowcase defaultShowcase = new UserAchievementShowcase();
        defaultShowcase.setUsername("user-2");
        defaultShowcase.setAchievementIds(List.of("ach-3"));

        assertAll("Verify UserAchievementShowcase properties",
                () -> assertEquals(USER_1, showcase.getUsername(), "Username should match USER_1"),
                () -> assertEquals(2, showcase.getAchievementIds().size(), "Showcase achievementIds list size should match 2"),
                () -> assertEquals("user-2", defaultShowcase.getUsername(), "Default showcase username should match user-2"),
                () -> assertEquals("ach-3", defaultShowcase.getAchievementIds().get(0), "Default showcase first achievementId should match ach-3"));
    }

    @Test
    void testUserDailyMissionProgress() {
        DailyMission mission = new CountBasedDailyMission();
        UserDailyMissionProgress p = new UserDailyMissionProgress();
        p.setId("up-1");
        p.setUsername(USER_1);
        p.setDailyMission(mission);
        p.setProgressDate(LocalDate.now());
        p.setProgressValue(3);
        p.setCompleted(true);
        LocalDateTime now = LocalDateTime.now();
        p.setCompletedAt(now);
        p.setCreatedAt(now);
        p.setUpdatedAt(now);

        assertAll("Verify UserDailyMissionProgress properties",
                () -> assertEquals("up-1", p.getId(), "ID should match up-1"),
                () -> assertEquals(USER_1, p.getUsername(), "Username should match USER_1"),
                () -> assertEquals(mission, p.getDailyMission(), "Daily mission reference should match"),
                () -> assertEquals(LocalDate.now(), p.getProgressDate(), "Progress date should match today"),
                () -> assertEquals(3, p.getProgressValue(), "Progress value should match 3"),
                () -> assertTrue(p.isCompleted(), "Completed flag should be true"),
                () -> assertEquals(now, p.getCompletedAt(), "CompletedAt timestamp should match now"),
                () -> assertEquals(now, p.getCreatedAt(), "CreatedAt timestamp should match now"),
                () -> assertEquals(now, p.getUpdatedAt(), "UpdatedAt timestamp should match now"));
    }
}
