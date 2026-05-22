package id.ac.ui.cs.advprog.yomu.gamification.mapper;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionResponse;
import id.ac.ui.cs.advprog.yomu.gamification.model.AccuracyBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.AccuracyDailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedDailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.RankingBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserDailyMissionProgress;

@SuppressWarnings("PMD")
class GamificationMapperImplTest {

    private static final String ACH_ID = "ach-id-123";
    private static final String ACH_NAME = "Master Reader";
    private static final String ACH_MILESTONE = "Read 5 books";
    private static final String MILESTONE_TYPE_COUNT = "count_based";
    private static final String MILESTONE_TYPE_ACC = "accuracy_above";
    private static final String MILESTONE_TYPE_RANK = "ranking_achieved";
    private static final String TIER_GOLD = "GOLD";
    
    private static final String MIS_ID = "mis-id-123";
    private static final String MIS_NAME = "Read 3 Articles";
    private static final String MIS_MILESTONE = "Read 3 articles today";
    private static final String MISSION_TYPE_READ = "read_n_articles";
    private static final String USERNAME = "user-123";

    // Assert messages
    private static final String MSG_ID = "Id should match";
    private static final String MSG_NAME = "Name should match";
    private static final String MSG_MILESTONE = "Milestone should match";
    private static final String MSG_MILESTONE_TYPE = "Milestone type should match";
    private static final String MSG_THRESHOLD = "Threshold should match";
    private static final String MSG_ACTIVE = "Active status should match";
    private static final String MSG_ACCURACY_THRESHOLD = "Accuracy threshold should match";
    private static final String MSG_TARGET_TIER = "Target tier should match";
    private static final String MSG_TIER = "Tier should match";
    private static final String MSG_TARGET_COUNT = "Target count should match";
    private static final String MSG_REQUIRED_COUNT = "Required count should match";
    private static final String MSG_REWARD_SCORE = "Reward score should match";
    private static final String MSG_USERNAME = "Username should match";
    private static final String MSG_PROGRESS_VALUE = "Progress value should match";
    private static final String MSG_COMPLETED = "Completed status should match";
    private static final String MSG_UNLOCKED = "Unlocked status should match";

    private GamificationMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new GamificationMapperImpl();
    }

    @Test
    void toAchievementResponse_WhenCountBased_ShouldMapCorrectly() {
        CountBasedAchievement achievement = new CountBasedAchievement();
        achievement.setId(ACH_ID);
        achievement.setName(ACH_NAME);
        achievement.setMilestone(ACH_MILESTONE);
        achievement.setMilestoneType(MILESTONE_TYPE_COUNT);
        achievement.setMilestoneThreshold(5);
        achievement.setTier(TIER_GOLD);
        achievement.setActive(true);

        AchievementResponse response = mapper.toAchievementResponse(achievement);

        assertAll("Verify toAchievementResponse for CountBasedAchievement",
                () -> assertEquals(ACH_ID, response.id(), MSG_ID),
                () -> assertEquals(ACH_NAME, response.name(), MSG_NAME),
                () -> assertEquals(ACH_MILESTONE, response.milestone(), MSG_MILESTONE),
                () -> assertEquals(MILESTONE_TYPE_COUNT, response.milestoneType(), MSG_MILESTONE_TYPE),
                () -> assertEquals(5, response.milestoneThreshold(), MSG_THRESHOLD),
                () -> assertNull(response.accuracyThreshold(), "Accuracy threshold should be null"),
                () -> assertEquals(TIER_GOLD, response.tier(), MSG_TIER),
                () -> assertNull(response.targetTier(), "Target tier should be null"),
                () -> assertTrue(response.active(), MSG_ACTIVE)
        );
    }

    @Test
    void toAchievementResponse_WhenAccuracyBased_ShouldMapCorrectly() {
        AccuracyBasedAchievement achievement = new AccuracyBasedAchievement();
        achievement.setId(ACH_ID);
        achievement.setName(ACH_NAME);
        achievement.setMilestone(ACH_MILESTONE);
        achievement.setMilestoneType(MILESTONE_TYPE_ACC);
        achievement.setMilestoneThreshold(3);
        achievement.setAccuracyThreshold(90);
        achievement.setTier(TIER_GOLD);
        achievement.setActive(true);

        AchievementResponse response = mapper.toAchievementResponse(achievement);

        assertAll("Verify toAchievementResponse for AccuracyBasedAchievement",
                () -> assertEquals(ACH_ID, response.id(), MSG_ID),
                () -> assertEquals(ACH_NAME, response.name(), MSG_NAME),
                () -> assertEquals(ACH_MILESTONE, response.milestone(), MSG_MILESTONE),
                () -> assertEquals(MILESTONE_TYPE_ACC, response.milestoneType(), MSG_MILESTONE_TYPE),
                () -> assertEquals(3, response.milestoneThreshold(), MSG_THRESHOLD),
                () -> assertEquals(90, response.accuracyThreshold(), MSG_ACCURACY_THRESHOLD),
                () -> assertEquals(TIER_GOLD, response.tier(), MSG_TIER),
                () -> assertNull(response.targetTier(), "Target tier should be null"),
                () -> assertTrue(response.active(), MSG_ACTIVE)
        );
    }

    @Test
    void toAchievementResponse_WhenRankingBased_ShouldMapCorrectly() {
        RankingBasedAchievement achievement = new RankingBasedAchievement();
        achievement.setId(ACH_ID);
        achievement.setName(ACH_NAME);
        achievement.setMilestone(ACH_MILESTONE);
        achievement.setMilestoneType(MILESTONE_TYPE_RANK);
        achievement.setMilestoneThreshold(1);
        achievement.setTargetTier(TIER_GOLD);
        achievement.setTier(TIER_GOLD);
        achievement.setActive(true);

        AchievementResponse response = mapper.toAchievementResponse(achievement);

        assertAll("Verify toAchievementResponse for RankingBasedAchievement",
                () -> assertEquals(ACH_ID, response.id(), MSG_ID),
                () -> assertEquals(ACH_NAME, response.name(), MSG_NAME),
                () -> assertEquals(ACH_MILESTONE, response.milestone(), MSG_MILESTONE),
                () -> assertEquals(MILESTONE_TYPE_RANK, response.milestoneType(), MSG_MILESTONE_TYPE),
                () -> assertEquals(1, response.milestoneThreshold(), MSG_THRESHOLD),
                () -> assertNull(response.accuracyThreshold(), "Accuracy threshold should be null"),
                () -> assertEquals(TIER_GOLD, response.tier(), MSG_TIER),
                () -> assertEquals(TIER_GOLD, response.targetTier(), MSG_TARGET_TIER),
                () -> assertTrue(response.active(), MSG_ACTIVE)
        );
    }

    @Test
    void toDailyMissionResponse_WhenCountBased_ShouldMapCorrectly() {
        CountBasedDailyMission mission = new CountBasedDailyMission();
        mission.setId(MIS_ID);
        mission.setName(MIS_NAME);
        mission.setMilestone(MIS_MILESTONE);
        mission.setMissionType(MISSION_TYPE_READ);
        mission.setTargetCount(5);
        mission.setRewardScore(50);
        mission.setActive(true);

        DailyMissionResponse response = mapper.toDailyMissionResponse(mission);

        assertAll("Verify toDailyMissionResponse for CountBasedDailyMission",
                () -> assertEquals(MIS_ID, response.id(), MSG_ID),
                () -> assertEquals(MIS_NAME, response.name(), MSG_NAME),
                () -> assertEquals(MIS_MILESTONE, response.milestone(), MSG_MILESTONE),
                () -> assertEquals(MISSION_TYPE_READ, response.missionType(), MSG_MILESTONE_TYPE),
                () -> assertEquals(5, response.targetCount(), MSG_TARGET_COUNT),
                () -> assertNull(response.accuracyThreshold(), "Accuracy threshold should be null"),
                () -> assertNull(response.requiredCount(), "Required count should be null"),
                () -> assertEquals(50, response.rewardScore(), MSG_REWARD_SCORE),
                () -> assertTrue(response.active(), MSG_ACTIVE)
        );
    }

    @Test
    void toDailyMissionResponse_WhenAccuracyBased_ShouldMapCorrectly() {
        AccuracyDailyMission mission = new AccuracyDailyMission();
        mission.setId(MIS_ID);
        mission.setName(MIS_NAME);
        mission.setMilestone(MIS_MILESTONE);
        mission.setMissionType(MISSION_TYPE_READ);
        mission.setAccuracyThreshold(85);
        mission.setRequiredCount(3);
        mission.setRewardScore(100);
        mission.setActive(true);

        DailyMissionResponse response = mapper.toDailyMissionResponse(mission);

        assertAll("Verify toDailyMissionResponse for AccuracyDailyMission",
                () -> assertEquals(MIS_ID, response.id(), MSG_ID),
                () -> assertEquals(MIS_NAME, response.name(), MSG_NAME),
                () -> assertEquals(MIS_MILESTONE, response.milestone(), MSG_MILESTONE),
                () -> assertEquals(MISSION_TYPE_READ, response.missionType(), MSG_MILESTONE_TYPE),
                () -> assertNull(response.targetCount(), "Target count should be null"),
                () -> assertEquals(85, response.accuracyThreshold(), MSG_ACCURACY_THRESHOLD),
                () -> assertEquals(3, response.requiredCount(), MSG_REQUIRED_COUNT),
                () -> assertEquals(100, response.rewardScore(), MSG_REWARD_SCORE),
                () -> assertTrue(response.active(), MSG_ACTIVE)
        );
    }

    @Test
    void toAchievementProgressResponse_WhenCountBased_ShouldMapCorrectly() {
        CountBasedAchievement achievement = new CountBasedAchievement();
        achievement.setId(ACH_ID);
        achievement.setName(ACH_NAME);
        achievement.setMilestone(ACH_MILESTONE);
        achievement.setMilestoneType(MILESTONE_TYPE_COUNT);
        achievement.setMilestoneThreshold(10);
        achievement.setTier(TIER_GOLD);

        UserAchievementProgress progress = new UserAchievementProgress();
        progress.setUsername(USERNAME);
        progress.setAchievement(achievement);
        progress.setProgressValue(5);
        progress.setUnlocked(false);

        AchievementProgressResponse response = mapper.toAchievementProgressResponse(progress);

        assertAll("Verify toAchievementProgressResponse for CountBasedAchievement",
                () -> assertEquals(ACH_ID, response.achievementId(), MSG_ID),
                () -> assertEquals(ACH_NAME, response.achievementName(), MSG_NAME),
                () -> assertEquals(USERNAME, response.username(), MSG_USERNAME),
                () -> assertEquals(5, response.progressValue(), MSG_PROGRESS_VALUE),
                () -> assertEquals(false, response.unlocked(), MSG_UNLOCKED),
                () -> assertEquals(ACH_MILESTONE, response.milestone(), MSG_MILESTONE),
                () -> assertEquals(MILESTONE_TYPE_COUNT, response.milestoneType(), MSG_MILESTONE_TYPE),
                () -> assertEquals(10, response.milestoneThreshold(), MSG_THRESHOLD),
                () -> assertNull(response.accuracyThreshold(), "Accuracy threshold should be null"),
                () -> assertEquals(TIER_GOLD, response.tier(), MSG_TIER),
                () -> assertNull(response.targetTier(), "Target tier should be null")
        );
    }

    @Test
    void toAchievementProgressResponse_WhenAccuracyBased_ShouldMapCorrectly() {
        AccuracyBasedAchievement achievement = new AccuracyBasedAchievement();
        achievement.setId(ACH_ID);
        achievement.setName(ACH_NAME);
        achievement.setMilestone(ACH_MILESTONE);
        achievement.setMilestoneType(MILESTONE_TYPE_ACC);
        achievement.setMilestoneThreshold(2);
        achievement.setAccuracyThreshold(95);
        achievement.setTier(TIER_GOLD);

        UserAchievementProgress progress = new UserAchievementProgress();
        progress.setUsername(USERNAME);
        progress.setAchievement(achievement);
        progress.setProgressValue(1);
        progress.setUnlocked(false);

        AchievementProgressResponse response = mapper.toAchievementProgressResponse(progress);

        assertAll("Verify toAchievementProgressResponse for AccuracyBasedAchievement",
                () -> assertEquals(ACH_ID, response.achievementId(), MSG_ID),
                () -> assertEquals(ACH_NAME, response.achievementName(), MSG_NAME),
                () -> assertEquals(USERNAME, response.username(), MSG_USERNAME),
                () -> assertEquals(1, response.progressValue(), MSG_PROGRESS_VALUE),
                () -> assertEquals(false, response.unlocked(), MSG_UNLOCKED),
                () -> assertEquals(ACH_MILESTONE, response.milestone(), MSG_MILESTONE),
                () -> assertEquals(MILESTONE_TYPE_ACC, response.milestoneType(), MSG_MILESTONE_TYPE),
                () -> assertEquals(2, response.milestoneThreshold(), MSG_THRESHOLD),
                () -> assertEquals(95, response.accuracyThreshold(), MSG_ACCURACY_THRESHOLD),
                () -> assertEquals(TIER_GOLD, response.tier(), MSG_TIER),
                () -> assertNull(response.targetTier(), "Target tier should be null")
        );
    }

    @Test
    void toAchievementProgressResponse_WhenRankingBased_ShouldMapCorrectly() {
        RankingBasedAchievement achievement = new RankingBasedAchievement();
        achievement.setId(ACH_ID);
        achievement.setName(ACH_NAME);
        achievement.setMilestone(ACH_MILESTONE);
        achievement.setMilestoneType(MILESTONE_TYPE_RANK);
        achievement.setMilestoneThreshold(1);
        achievement.setTargetTier(TIER_GOLD);
        achievement.setTier(TIER_GOLD);

        UserAchievementProgress progress = new UserAchievementProgress();
        progress.setUsername(USERNAME);
        progress.setAchievement(achievement);
        progress.setProgressValue(1);
        progress.setUnlocked(true);

        AchievementProgressResponse response = mapper.toAchievementProgressResponse(progress);

        assertAll("Verify toAchievementProgressResponse for RankingBasedAchievement",
                () -> assertEquals(ACH_ID, response.achievementId(), MSG_ID),
                () -> assertEquals(ACH_NAME, response.achievementName(), MSG_NAME),
                () -> assertEquals(USERNAME, response.username(), MSG_USERNAME),
                () -> assertEquals(1, response.progressValue(), MSG_PROGRESS_VALUE),
                () -> assertEquals(true, response.unlocked(), MSG_UNLOCKED),
                () -> assertEquals(ACH_MILESTONE, response.milestone(), MSG_MILESTONE),
                () -> assertEquals(MILESTONE_TYPE_RANK, response.milestoneType(), MSG_MILESTONE_TYPE),
                () -> assertEquals(1, response.milestoneThreshold(), MSG_THRESHOLD),
                () -> assertNull(response.accuracyThreshold(), "Accuracy threshold should be null"),
                () -> assertEquals(TIER_GOLD, response.tier(), MSG_TIER),
                () -> assertEquals(TIER_GOLD, response.targetTier(), MSG_TARGET_TIER)
        );
    }

    @Test
    void toDailyMissionProgressResponse_WhenCountBased_ShouldMapCorrectly() {
        CountBasedDailyMission mission = new CountBasedDailyMission();
        mission.setId(MIS_ID);
        mission.setName(MIS_NAME);
        mission.setMilestone(MIS_MILESTONE);
        mission.setMissionType(MISSION_TYPE_READ);
        mission.setTargetCount(10);
        mission.setRewardScore(75);

        UserDailyMissionProgress progress = new UserDailyMissionProgress();
        progress.setUsername(USERNAME);
        progress.setDailyMission(mission);
        progress.setProgressDate(LocalDate.of(2026, 5, 22));
        progress.setProgressValue(6);
        progress.setCompleted(false);

        DailyMissionProgressResponse response = mapper.toDailyMissionProgressResponse(progress);

        assertAll("Verify toDailyMissionProgressResponse for CountBasedDailyMission",
                () -> assertEquals(MIS_ID, response.dailyMissionId(), MSG_ID),
                () -> assertEquals(MIS_NAME, response.dailyMissionName(), MSG_NAME),
                () -> assertEquals(USERNAME, response.username(), MSG_USERNAME),
                () -> assertEquals(LocalDate.of(2026, 5, 22), response.progressDate(), "Progress date should match"),
                () -> assertEquals(6, response.progressValue(), MSG_PROGRESS_VALUE),
                () -> assertEquals(false, response.completed(), MSG_COMPLETED),
                () -> assertEquals(MIS_MILESTONE, response.milestone(), MSG_MILESTONE),
                () -> assertEquals(10, response.targetCount(), MSG_TARGET_COUNT),
                () -> assertNull(response.accuracyThreshold(), "Accuracy threshold should be null"),
                () -> assertNull(response.requiredCount(), "Required count should be null"),
                () -> assertEquals(75, response.rewardScore(), MSG_REWARD_SCORE),
                () -> assertEquals(MISSION_TYPE_READ, response.missionType(), MSG_MILESTONE_TYPE)
        );
    }

    @Test
    void toDailyMissionProgressResponse_WhenAccuracyBased_ShouldMapCorrectly() {
        AccuracyDailyMission mission = new AccuracyDailyMission();
        mission.setId(MIS_ID);
        mission.setName(MIS_NAME);
        mission.setMilestone(MIS_MILESTONE);
        mission.setMissionType(MISSION_TYPE_READ);
        mission.setAccuracyThreshold(80);
        mission.setRequiredCount(5);
        mission.setRewardScore(150);

        UserDailyMissionProgress progress = new UserDailyMissionProgress();
        progress.setUsername(USERNAME);
        progress.setDailyMission(mission);
        progress.setProgressDate(LocalDate.of(2026, 5, 22));
        progress.setProgressValue(80);
        progress.setCompleted(true);

        DailyMissionProgressResponse response = mapper.toDailyMissionProgressResponse(progress);

        assertAll("Verify toDailyMissionProgressResponse for AccuracyDailyMission",
                () -> assertEquals(MIS_ID, response.dailyMissionId(), MSG_ID),
                () -> assertEquals(MIS_NAME, response.dailyMissionName(), MSG_NAME),
                () -> assertEquals(USERNAME, response.username(), MSG_USERNAME),
                () -> assertEquals(LocalDate.of(2026, 5, 22), response.progressDate(), "Progress date should match"),
                () -> assertEquals(80, response.progressValue(), MSG_PROGRESS_VALUE),
                () -> assertEquals(true, response.completed(), MSG_COMPLETED),
                () -> assertEquals(MIS_MILESTONE, response.milestone(), MSG_MILESTONE),
                () -> assertNull(response.targetCount(), "Target count should be null"),
                () -> assertEquals(80, response.accuracyThreshold(), MSG_ACCURACY_THRESHOLD),
                () -> assertEquals(5, response.requiredCount(), MSG_REQUIRED_COUNT),
                () -> assertEquals(150, response.rewardScore(), MSG_REWARD_SCORE),
                () -> assertEquals(MISSION_TYPE_READ, response.missionType(), MSG_MILESTONE_TYPE)
        );
    }
}
