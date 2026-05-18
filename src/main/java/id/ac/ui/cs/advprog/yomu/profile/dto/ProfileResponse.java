package id.ac.ui.cs.advprog.yomu.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {
    private String userId;
    private String username;
    private String displayName;
    private String bio;
    private String joinedDate;
    private String clanName;
    private String clanTier;
    private ReadingStatsDto readingStats;
    private List<ShowcaseAchievementDto> showcaseAchievements;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReadingStatsDto {
        private int completedTexts;
        private int totalMinutes;
        private int quizAccuracy;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShowcaseAchievementDto {
        private String id;
        private String name;
        private String description;
        private String tier;
    }
}
