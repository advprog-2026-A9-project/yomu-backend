package id.ac.ui.cs.advprog.yomu.social.dto;

public record ClanMemberDTO(
        String userId,
        String username,
        String role,
        int contribution,
        int streak,
        boolean isOnline
) {}
