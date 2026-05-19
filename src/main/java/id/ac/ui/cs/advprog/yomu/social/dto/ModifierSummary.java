package id.ac.ui.cs.advprog.yomu.social.dto;

import java.util.List;

public record ModifierSummary(
        List<ClanModifierDTO> buffs,
        List<ClanModifierDTO> debuffs,
        double multiplier
) {}
