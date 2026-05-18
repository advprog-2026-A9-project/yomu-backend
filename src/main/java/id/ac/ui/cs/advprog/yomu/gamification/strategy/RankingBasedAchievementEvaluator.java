package id.ac.ui.cs.advprog.yomu.gamification.strategy;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;

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
        int rank = (context instanceof Integer) ? (Integer) context : Integer.MAX_VALUE;
        if (rank <= progress.getAchievement().getMilestoneThreshold()) {
            progress.setProgressValue(1);
            progress.setUnlocked(true);
            progress.setUnlockedAt(LocalDateTime.now());
            return true;
        }
        return false;
    }
}
