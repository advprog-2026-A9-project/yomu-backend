package id.ac.ui.cs.advprog.yomu.gamification.model;

import java.util.Locale;

public enum DailyMissionType {
    READ_N_ARTICLES("read_n_articles"),
    COMPLETE_N_QUIZZES("complete_n_quizzes"),
    ACHIEVE_ACCURACY("achieve_accuracy");

    private final String value;

    DailyMissionType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static DailyMissionType from(String s) {
        if (s == null) return null;
        String normalized = s.trim().toLowerCase(Locale.ROOT);
        for (DailyMissionType t : values()) {
            if (t.value.equals(normalized)) return t;
        }
        return null;
    }
}
