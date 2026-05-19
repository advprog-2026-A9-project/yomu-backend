package id.ac.ui.cs.advprog.yomu.social.dto;

import java.util.List;

public record ClanDetailResponse(
        String id,
        String name,
        String description,
        String leaderUsername,
        String tier,
        int rank,
        int score,
        int memberCount,
        int maxMembers,
        double avgAccuracy,
        List<ClanMemberDTO> members,
        List<ClanModifierDTO> activeBuffs,
        List<ClanModifierDTO> debuffs
) {}
