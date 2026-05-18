package id.ac.ui.cs.advprog.yomu.gamification.strategy;

import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;

public interface AchievementProgressEvaluator {
    boolean supports(String milestoneType);
    boolean evaluate(UserAchievementProgress progress, Object context);
}
