package id.ac.ui.cs.advprog.yomu.gamification.strategy;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import id.ac.ui.cs.advprog.yomu.gamification.model.RankingBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;

@SuppressWarnings("PMD")
class RankingBasedAchievementEvaluatorTest {

    private RankingBasedAchievementEvaluator evaluator;
    private RankingBasedAchievement achievement;
    private UserAchievementProgress progress;

    @BeforeEach
    void setUp() {
        evaluator = new RankingBasedAchievementEvaluator();

        achievement = new RankingBasedAchievement();
        achievement.setId("ach-1");
        achievement.setName("Diamond Top Clan");
        achievement.setMilestoneType("ranking_achieved");
        achievement.setMilestoneThreshold(3); // Rank <= 3
        achievement.setTargetTier("DIAMOND");

        progress = new UserAchievementProgress();
        progress.setUsername("user1");
        progress.setAchievement(achievement);
        progress.setProgressValue(0);
        progress.setUnlocked(false);
    }

    @Test
    void testSupports() {
        assertTrue(evaluator.supports("ranking_achieved"));
        assertFalse(evaluator.supports("accuracy_above"));
        assertFalse(evaluator.supports("other_type"));
    }

    @Test
    void testEvaluate_WhenAlreadyUnlocked_ShouldReturnFalse() {
        progress.setUnlocked(true);
        RankingContext context = new RankingContext(1, "DIAMOND");
        assertFalse(evaluator.evaluate(progress, context));
    }

    @Test
    void testEvaluate_WhenInvalidContext_ShouldReturnFalse() {
        assertFalse(evaluator.evaluate(progress, "not-a-ranking-context"));
    }

    @Test
    void testEvaluate_WhenTierMismatch_ShouldReturnFalse() {
        RankingContext context = new RankingContext(1, "GOLD"); // Gold tier, but achievement requires Diamond
        assertFalse(evaluator.evaluate(progress, context));
        assertFalse(progress.isUnlocked());
    }

    @Test
    void testEvaluate_WhenTierMatchesAndRankWithinThreshold_ShouldUnlock() {
        RankingContext context = new RankingContext(2, "DIAMOND"); // Rank 2 <= 3
        assertTrue(evaluator.evaluate(progress, context));
        assertTrue(progress.isUnlocked());
        assertTrue(progress.getProgressValue() == 1);
    }

    @Test
    void testEvaluate_WhenTierMatchesButRankExceedsThreshold_ShouldNotUnlock() {
        RankingContext context = new RankingContext(4, "DIAMOND"); // Rank 4 > 3
        assertFalse(evaluator.evaluate(progress, context));
        assertFalse(progress.isUnlocked());
    }

    @Test
    void testEvaluate_WhenTargetTierIsNull_ShouldMatchAnyTier() {
        achievement.setTargetTier(null);
        RankingContext context = new RankingContext(1, "MYTHIC");
        assertTrue(evaluator.evaluate(progress, context));
        assertTrue(progress.isUnlocked());
    }
}
