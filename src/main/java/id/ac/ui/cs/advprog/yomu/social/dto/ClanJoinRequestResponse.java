package id.ac.ui.cs.advprog.yomu.social.dto;

import java.time.LocalDateTime;

public record ClanJoinRequestResponse(
        Long id,
        String clanId,
        String username,
        String status,
        LocalDateTime createdAt
) {}
