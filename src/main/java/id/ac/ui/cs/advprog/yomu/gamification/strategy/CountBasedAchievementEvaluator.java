package id.ac.ui.cs.advprog.yomu.gamification.strategy;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.gamification.model.AchievementMilestoneType;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;

@Component
public class CountBasedAchievementEvaluator implements AchievementProgressEvaluator {

    @Override
    public boolean supports(String milestoneType) {
        AchievementMilestoneType t = AchievementMilestoneType.from(milestoneType);
        return t == AchievementMilestoneType.READINGS_COMPLETED || t == AchievementMilestoneType.QUIZZES_PASSED;
    }

    @Override
    public boolean evaluate(UserAchievementProgress progress, Object context) {
        if (progress.isUnlocked()) {
            return false;
        }

        String milestoneType = progress.getAchievement().getMilestoneType();
        AchievementMilestoneType milestone = AchievementMilestoneType.from(milestoneType);
        int increment = 0;
        if (milestone == AchievementMilestoneType.READINGS_COMPLETED) {
            if (context instanceof ReadingCompletionContext) {
                increment = 1;
            } else if (context instanceof Integer integerVal) {
                increment = integerVal;
            }
        } else if (milestone == AchievementMilestoneType.QUIZZES_PASSED) {
            if (context instanceof QuizCompletionContext) {
                increment = 1;
            } else if (context instanceof Integer integerVal) {
                increment = integerVal;
            }
        }

        if (increment <= 0) {
            return false;
        }

        progress.setProgressValue(progress.getProgressValue() + increment);

        if (progress.getProgressValue() >= progress.getAchievement().getMilestoneThreshold()) {
            progress.setUnlocked(true);
            progress.setUnlockedAt(LocalDateTime.now());
        }
        return true;
    }
}
