package id.ac.ui.cs.advprog.yomu.gamification.strategy;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;

@Component
public class AccuracyBasedAchievementEvaluator implements AchievementProgressEvaluator {

    @Override
    public boolean supports(String milestoneType) {
        return "accuracy_above".equals(milestoneType);
    }

    @Override
    public boolean evaluate(UserAchievementProgress progress, Object context) {
        if (progress.isUnlocked()) {
            return false;
        }
        int accuracy = (context instanceof Integer) ? (Integer) context : 0;
        if (accuracy >= progress.getAchievement().getMilestoneThreshold()) {
            progress.setProgressValue(1);
            progress.setUnlocked(true);
            progress.setUnlockedAt(LocalDateTime.now());
            return true;
        }
        return false;
    }
}
