package id.ac.ui.cs.advprog.yomu.social.dto;

/**
 * DTO representing a clan's position in the leaderboard.
 */
public record LeaderboardEntryResponse(
        String clanId,
        String clanName,
        String tier,
        int score,
        int rank,
        int memberCount
) {}
