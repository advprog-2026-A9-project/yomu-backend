package id.ac.ui.cs.advprog.yomu.social.dto;

public record MyClanResponse(
        String id,
        String name,
        String description,
        String leaderUserId,
        String role,
        int members
) {
}
