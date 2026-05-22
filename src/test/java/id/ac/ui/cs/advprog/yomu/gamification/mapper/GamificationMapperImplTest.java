package id.ac.ui.cs.advprog.yomu.gamification.mapper;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionResponse;
import id.ac.ui.cs.advprog.yomu.gamification.model.AccuracyBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.AccuracyDailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedDailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.RankingBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserDailyMissionProgress;

class GamificationMapperImplTest {

    private final GamificationMapperImpl mapper = new GamificationMapperImpl();

    @Test
    void toAchievementResponse_accuracyAndRankingHandled() {
        AccuracyBasedAchievement a = new AccuracyBasedAchievement();
        a.setId("ach-1");
        a.setName("Accuracy Ace");
        a.setMilestone("Do well");
        a.setMilestoneType("COUNT");
        a.setMilestoneThreshold(10);
        a.setAccuracyThreshold(80);
        a.setTier("GOLD");

        AchievementResponse resp = mapper.toAchievementResponse(a);

        RankingBasedAchievement r = new RankingBasedAchievement();
        r.setId("ach-2");
        r.setName("Ranker");
        r.setMilestone("Be top");
        r.setMilestoneType("RANK");
        r.setMilestoneThreshold(1);
        r.setTargetTier("PLATINUM");

        AchievementResponse rr = mapper.toAchievementResponse(r);

        assertAll("Verify achievement mapping",
                () -> assertEquals("ach-1", resp.id(), "Response ID should match"),
                () -> assertEquals(Integer.valueOf(80), resp.accuracyThreshold(), "Accuracy threshold should match"),
                () -> assertEquals("PLATINUM", rr.targetTier(), "Target tier should match"));
    }

    @Test
    void toDailyMissionResponse_countAndAccuracyHandled() {
        CountBasedDailyMission c = new CountBasedDailyMission();
        c.setId("dm-1");
        c.setName("CountIt");
        c.setMilestone("Answer");
        c.setMissionType("COUNT");
        c.setTargetCount(5);
        c.setRewardScore(10);

        DailyMissionResponse dr = mapper.toDailyMissionResponse(c);

        AccuracyDailyMission a = new AccuracyDailyMission();
        a.setId("dm-2");
        a.setName("Accu");
        a.setMilestone("Acc milestone");
        a.setMissionType("ACCURACY");
        a.setAccuracyThreshold(70);
        a.setRequiredCount(7);

        DailyMissionResponse ar = mapper.toDailyMissionResponse(a);

        assertAll("Verify daily mission mapping",
                () -> assertEquals(Integer.valueOf(5), dr.targetCount(), "Target count should match"),
                () -> assertEquals(Integer.valueOf(70), ar.accuracyThreshold(), "Accuracy threshold should match"),
                () -> assertEquals(Integer.valueOf(7), ar.requiredCount(), "Required count should match"));
    }

    @Test
    void progressResponses_mapCorrectly() {
        Achievement base = new Achievement();
        base.setId("ach-3");
        base.setName("Base");
        base.setMilestone("M");
        base.setMilestoneType("T");
        base.setMilestoneThreshold(3);

        UserAchievementProgress u = new UserAchievementProgress();
        u.setUsername("bob");
        u.setAchievement(base);
        u.setProgressValue(2);
        u.setUnlocked(false);

        AchievementProgressResponse apr = mapper.toAchievementProgressResponse(u);

        CountBasedDailyMission dm = new CountBasedDailyMission();
        dm.setId("dm-3");
        dm.setName("Daily");
        dm.setMilestone("milestone");
        dm.setMissionType("COUNT");
        dm.setTargetCount(3);

        UserDailyMissionProgress udp = new UserDailyMissionProgress();
        udp.setUsername("carol");
        udp.setDailyMission(dm);
        udp.setProgressDate(LocalDate.now());
        udp.setProgressValue(1);
        udp.setCompleted(false);

        DailyMissionProgressResponse dmpr = mapper.toDailyMissionProgressResponse(udp);

        assertAll("Verify progress mapping responses",
                () -> assertEquals("bob", apr.username(), "Username should match bob"),
                () -> assertEquals(2, apr.progressValue(), "Progress value should match 2"),
                () -> assertEquals("carol", dmpr.username(), "Username should match carol"),
                () -> assertEquals(1, dmpr.progressValue(), "Progress value should match 1"));
    }
}
