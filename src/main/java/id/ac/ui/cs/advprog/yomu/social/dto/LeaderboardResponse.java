package id.ac.ui.cs.advprog.yomu.social.dto;

import java.util.List;

/**
 * DTO for grouped leaderboard by tier.
 */
public record LeaderboardResponse(
        String tier,
        List<LeaderboardEntryResponse> entries
) {}
