package id.ac.ui.cs.advprog.yomu.social.model;

public enum Tier {
    BRONZE(1, "Bronze"),
    SILVER(2, "Silver"),
    GOLD(3, "Gold"),
    DIAMOND(4, "Diamond");

    private final int level;
    private final String displayName;

    Tier(int level, String displayName) {
        this.level = level;
        this.displayName = displayName;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Tier promote() {
        return switch (this) {
            case BRONZE -> SILVER;
            case SILVER -> GOLD;
            case GOLD -> DIAMOND;
            case DIAMOND -> DIAMOND; // Already at top
        };
    }

    public Tier demote() {
        return switch (this) {
            case DIAMOND -> GOLD;
            case GOLD -> SILVER;
            case SILVER -> BRONZE;
            case BRONZE -> BRONZE; // Already at bottom
        };
    }
}
