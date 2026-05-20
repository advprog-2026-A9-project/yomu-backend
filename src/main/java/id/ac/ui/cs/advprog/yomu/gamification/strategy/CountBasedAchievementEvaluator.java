package id.ac.ui.cs.advprog.yomu.gamification.strategy;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;

@Component
public class CountBasedAchievementEvaluator implements AchievementProgressEvaluator {

    private static final String MILESTONE_READINGS_COMPLETED = "readings_completed";
    private static final String MILESTONE_QUIZZES_PASSED = "quizzes_passed";

    @Override
    public boolean supports(String milestoneType) {
        return MILESTONE_READINGS_COMPLETED.equals(milestoneType)
                || MILESTONE_QUIZZES_PASSED.equals(milestoneType);
    }

    @Override
    public boolean evaluate(UserAchievementProgress progress, Object context) {
        if (progress.isUnlocked()) {
            return false;
        }

        String milestoneType = progress.getAchievement().getMilestoneType();
        int increment = 0;

        if (MILESTONE_READINGS_COMPLETED.equals(milestoneType)) {
            if (context instanceof ReadingCompletionContext) {
                increment = 1;
            } else if (context instanceof Integer integerVal) {
                increment = integerVal;
            }
        } else if (MILESTONE_QUIZZES_PASSED.equals(milestoneType)) {
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
