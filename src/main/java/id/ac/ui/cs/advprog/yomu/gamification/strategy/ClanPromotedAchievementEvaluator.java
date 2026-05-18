package id.ac.ui.cs.advprog.yomu.gamification.strategy;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;

@Component
public class ClanPromotedAchievementEvaluator implements AchievementProgressEvaluator {

    @Override
    public boolean supports(String milestoneType) {
        return "clan_promoted".equals(milestoneType);
    }

    @Override
    public boolean evaluate(UserAchievementProgress progress, Object context) {
        if (progress.isUnlocked()) {
            return false;
        }
        boolean promoted = (context instanceof Boolean) ? (Boolean) context : false;
        if (promoted) {
            progress.setProgressValue(1);
            progress.setUnlocked(true);
            progress.setUnlockedAt(LocalDateTime.now());
            return true;
        }
        return false;
    }
}
