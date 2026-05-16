package id.ac.ui.cs.advprog.yomu.social.mapper;

import id.ac.ui.cs.advprog.yomu.social.dto.*;
import id.ac.ui.cs.advprog.yomu.social.model.*;

import java.util.List;

/**
 * Abstraction for entity-to-DTO mapping in the social module.
 * Dependency Inversion: high-level service modules depend on this interface.
 */
public interface SocialMapper {

    ClanSummaryResponse toClanSummaryResponse(ClanSummaryRow row, List<ClanModifierDTO> activeBuffs,
            List<ClanModifierDTO> debuffs, int effectiveScore);

    ClanDetailResponse toClanDetailResponse(Clan clan, int rank, int memberCount, List<ClanMemberDTO> memberDTOs,
            List<ClanModifierDTO> activeBuffs, List<ClanModifierDTO> debuffs);

    MyClanResponse toMyClanResponse(Clan clan, String role, int rank, List<ClanMember> members);

    ClanMemberDTO toClanMemberDTO(ClanMember member);

    ClanModifierDTO toClanModifierDTO(ClanModifier modifier);

    LeaderboardEntryResponse toLeaderboardEntryResponse(ClanLeaderboardRow row, int rank);

    LeaderboardEntryResponse toLeaderboardEntryResponse(Clan clan, int rank, int memberCount);

    SeasonStatusResponse toSeasonStatusResponse(SeasonState state);

    SeasonStatusResponse toDefaultSeasonStatusResponse();

    SeasonClanSummary toSeasonClanSummary(Clan clan, int memberCount);

    SeasonEndResponse toSeasonEndResponse(int currentSeasonNumber, int newSeasonNumber,
            List<SeasonClanSummary> promoted, List<SeasonClanSummary> relegated, List<SeasonClanSummary> unchanged,
            List<SeasonTierSummary> tierSummaries);
}
