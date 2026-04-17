package id.ac.ui.cs.advprog.yomu.social.dto;

import java.util.List;

import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;

public record MyClanResponse(
        String id,
        String name,
        String description,
        String leaderUserId,
        String role,
        List<ClanMember> members
) {
}
