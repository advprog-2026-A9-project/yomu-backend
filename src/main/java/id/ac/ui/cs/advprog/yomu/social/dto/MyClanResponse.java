package id.ac.ui.cs.advprog.yomu.social.dto;

import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;

import java.util.List;

public record MyClanResponse(
        String id,
        String name,
        String description,
        String leaderUserId,
        String role,
        List<ClanMember> members
) {
}
