package id.ac.ui.cs.advprog.yomu.social.dto;

import java.util.List;

public record ClanSummaryResponse(
        String id,
        String name,
        String description,
        String leaderUserId,
        String tier,
        int score,
        int effectiveScore,
        long memberCount,
        List<ClanModifierDTO> activeBuffs,
        List<ClanModifierDTO> debuffs
) {}
