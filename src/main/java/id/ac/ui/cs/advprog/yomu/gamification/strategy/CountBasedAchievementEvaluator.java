package id.ac.ui.cs.advprog.yomu.gamification.strategy;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;

@Component
public class CountBasedAchievementEvaluator implements AchievementProgressEvaluator {

    @Override
    public boolean supports(String milestoneType) {
        return "readings_completed".equals(milestoneType) || "quizzes_passed".equals(milestoneType);
    }

    @Override
    public boolean evaluate(UserAchievementProgress progress, Object context) {
        if (progress.isUnlocked()) {
            return false;
        }
        int increment = (context instanceof Integer) ? (Integer) context : 1;
        progress.setProgressValue(progress.getProgressValue() + increment);

        if (progress.getProgressValue() >= progress.getAchievement().getMilestoneThreshold()) {
            progress.setUnlocked(true);
            progress.setUnlockedAt(LocalDateTime.now());
            return true;
        }
        return false;
    }
}
