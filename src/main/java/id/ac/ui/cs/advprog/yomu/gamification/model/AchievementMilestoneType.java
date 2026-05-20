package id.ac.ui.cs.advprog.yomu.gamification.model;

import java.util.Locale;

public enum AchievementMilestoneType {
    READINGS_COMPLETED("readings_completed"),
    QUIZZES_PASSED("quizzes_passed"),
    ACCURACY_ABOVE("accuracy_above"),
    RANKING_ACHIEVED("ranking_achieved");

    private final String value;

    AchievementMilestoneType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static AchievementMilestoneType from(String s) {
        if (s == null) return null;
        String normalized = s.trim().toLowerCase(Locale.ROOT);
        for (AchievementMilestoneType t : values()) {
            if (t.value.equals(normalized)) return t;
        }
        return null;
    }
}
