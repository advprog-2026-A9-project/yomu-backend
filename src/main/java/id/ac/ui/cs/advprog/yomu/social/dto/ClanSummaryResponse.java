package id.ac.ui.cs.advprog.yomu.social.dto;

public record ClanSummaryResponse(
        String id,
        String name,
        String description,
        String leaderUserId,
        String tier,
        int score,
        long memberCount
) {}
