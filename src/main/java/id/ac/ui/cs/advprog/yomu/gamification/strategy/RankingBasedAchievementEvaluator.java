package id.ac.ui.cs.advprog.yomu.gamification.strategy;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;
import id.ac.ui.cs.advprog.yomu.gamification.model.RankingBasedAchievement;

@Component
public class RankingBasedAchievementEvaluator implements AchievementProgressEvaluator {

    @Override
    public boolean supports(String milestoneType) {
        return "ranking_achieved".equals(milestoneType);
    }

    @Override
    public boolean evaluate(UserAchievementProgress progress, Object context) {
        if (progress.isUnlocked()) {
            return false;
        }

        if (!(context instanceof RankingContext rankingContext)) {
            return false;
        }

        int rank = rankingContext.rank();
        String eventTier = rankingContext.tier();
        
        String targetTier = null;
        if (progress.getAchievement() instanceof RankingBasedAchievement rankingAchievement) {
            targetTier = rankingAchievement.getTargetTier();
        }

        // Tier filter: if achievement has a target tier requirement set, only match that tier
        if (targetTier != null && !targetTier.isBlank()
                && !targetTier.equalsIgnoreCase(eventTier)) {
            return false;
        }

        if (rank <= progress.getAchievement().getMilestoneThreshold()) {
            progress.setProgressValue(1);
            progress.setUnlocked(true);
            progress.setUnlockedAt(LocalDateTime.now());
            return true;
        }
        return false;
    }
}
