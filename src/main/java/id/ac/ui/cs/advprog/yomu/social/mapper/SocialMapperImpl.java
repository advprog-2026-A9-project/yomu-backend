package id.ac.ui.cs.advprog.yomu.social.mapper;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanDetailResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanLeaderboardRow;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanMemberDTO;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanModifierDTO;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryRow;
import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardEntryResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.MyClanResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.SeasonClanSummary;
import id.ac.ui.cs.advprog.yomu.social.dto.SeasonEndResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.SeasonStatusResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.SeasonTierSummary;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.model.ClanModifier;
import id.ac.ui.cs.advprog.yomu.social.model.SeasonState;

@Component
public class SocialMapperImpl implements SocialMapper {

    @Override
    public ClanSummaryResponse toClanSummaryResponse(ClanSummaryRow row, List<ClanModifierDTO> activeBuffs,
            List<ClanModifierDTO> debuffs, int effectiveScore) {
        return new ClanSummaryResponse(
                row.getClanId(),
                row.getClanName(),
                row.getDescription(),
                row.getLeaderUsername(),
                row.getTier().name(),
                row.getScore(),
                effectiveScore,
                row.getMemberCount(),
                activeBuffs,
                debuffs);
    }

    @Override
    public ClanDetailResponse toClanDetailResponse(Clan clan, int rank, int memberCount, List<ClanMemberDTO> memberDTOs,
            List<ClanModifierDTO> activeBuffs, List<ClanModifierDTO> debuffs) {
        return new ClanDetailResponse(
                clan.getId(),
                clan.getName(),
                clan.getDescription() == null ? "" : clan.getDescription(),
                clan.getLeaderUsername(),
                clan.getTier().name(),
                rank,
                clan.getScore(),
                memberCount,
                SocialConstants.MAX_CLAN_SIZE,
                0.0, // Default avg accuracy as per original logic
                memberDTOs,
                activeBuffs,
                debuffs);
    }

    @Override
    public MyClanResponse toMyClanResponse(Clan clan, String role, int rank, List<ClanMember> members) {
        return new MyClanResponse(
                clan.getId(),
                clan.getName(),
                clan.getDescription(),
                clan.getLeaderUsername(),
                role,
                clan.getTier().getDisplayName(),
                clan.getScore(),
                rank,
                members);
    }

    @Override
    public ClanMemberDTO toClanMemberDTO(ClanMember member) {
        return new ClanMemberDTO(
                member.getUsername(),
                member.getRole().toString(),
                0, // Default contribution
                0, // Default streak
                true // Default online status
        );
    }

    @Override
    public ClanModifierDTO toClanModifierDTO(ClanModifier modifier) {
        String displayName = modifier.getKey();
        if (SocialConstants.DAILY_MISSION_BUFF_KEY.equals(modifier.getKey())) {
            displayName = "Daily Mission Buff";
        } else if (SocialConstants.LOW_ACCURACY_PENALTY_KEY.equals(modifier.getKey())) {
            displayName = "Low Accuracy Penalty";
        }

        String description = modifier.getType().name() + " modifier";
        if (SocialConstants.DAILY_MISSION_BUFF_KEY.equals(modifier.getKey())) {
            description = "+20% Points Multiplier";
        } else if (SocialConstants.LOW_ACCURACY_PENALTY_KEY.equals(modifier.getKey())) {
            description = "-20% Points Multiplier";
        }

        return new ClanModifierDTO(
                displayName,
                "x" + String.format("%.2f", modifier.getMultiplier()),
                modifier.getType().name().toLowerCase(Locale.ROOT),
                modifier.getEndAt() == null ? "Active" : "Until " + modifier.getEndAt().toString(),
                description);
    }

    @Override
    public LeaderboardEntryResponse toLeaderboardEntryResponse(ClanLeaderboardRow row, int rank) {
        return new LeaderboardEntryResponse(
                row.getClanId(),
                row.getClanName(),
                row.getTier().getDisplayName(),
                row.getScore(),
                rank,
                Math.toIntExact(row.getMemberCount()));
    }

    @Override
    public LeaderboardEntryResponse toLeaderboardEntryResponse(Clan clan, int rank, int memberCount) {
        return new LeaderboardEntryResponse(
                clan.getId(),
                clan.getName(),
                clan.getTier().getDisplayName(),
                clan.getScore(),
                rank,
                memberCount);
    }

    @Override
    public SeasonStatusResponse toSeasonStatusResponse(SeasonState state) {
        return new SeasonStatusResponse(state.getSeasonNumber(), state.isActive() ? "Active" : "Ended");
    }

    @Override
    public SeasonStatusResponse toDefaultSeasonStatusResponse() {
        return new SeasonStatusResponse(1, "Active");
    }

    @Override
    public SeasonClanSummary toSeasonClanSummary(Clan clan, int memberCount) {
        return new SeasonClanSummary(
                clan.getId(),
                clan.getName(),
                clan.getTier().getDisplayName(),
                clan.getScore(),
                memberCount);
    }

    @Override
    public SeasonEndResponse toSeasonEndResponse(int currentSeasonNumber, int newSeasonNumber,
            List<SeasonClanSummary> promoted, List<SeasonClanSummary> relegated, List<SeasonClanSummary> unchanged,
            List<SeasonTierSummary> tierSummaries) {
        return new SeasonEndResponse(
                currentSeasonNumber,
                newSeasonNumber,
                promoted,
                relegated,
                unchanged,
                tierSummaries);
    }
}
