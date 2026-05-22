package id.ac.ui.cs.advprog.yomu.social.service.clan.query;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanDetailResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanLeaderboardRow;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanMemberDTO;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryRow;
import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardEntryResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.MyClanResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.ModifierSummary;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanRepository;
import id.ac.ui.cs.advprog.yomu.social.validation.ClanValidator;
import id.ac.ui.cs.advprog.yomu.social.mapper.SocialMapper;
import id.ac.ui.cs.advprog.yomu.social.service.modifier.ClanModifierService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClanQueryServiceImpl implements ClanQueryService {

    private final ClanRepository clanRepository;
    private final ClanMemberRepository memberRepository;
    private final ClanValidator clanValidator;
    private final SocialMapper socialMapper;
    private final ClanModifierService modifierService;

    private List<ClanSummaryResponse> mapToSummaryResponses(List<ClanSummaryRow> rows) {
        List<String> clanIds = rows.stream()
                .map(ClanSummaryRow::getClanId)
                .toList();

        var modifierSummaries = modifierService.getModifierSummaries(clanIds);

        return rows.stream()
                .map(row -> {
                    ModifierSummary mod = modifierSummaries.getOrDefault(row.getClanId(),
                            new ModifierSummary(List.of(), List.of(), 1.0));
                    int effectiveScore = (int) Math.round(row.getScore() * mod.multiplier());
                    return socialMapper.toClanSummaryResponse(row, mod.buffs(), mod.debuffs(), effectiveScore);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClanSummaryResponse> findAll(String search) {
        PageRequest pageRequest = PageRequest.of(0, 100);
        var rows = (search == null || search.isBlank())
                ? clanRepository.findAllClanSummaries(pageRequest)
                : clanRepository.findClanSummariesByQuery(search, pageRequest);

        return mapToSummaryResponses(rows);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClanSummaryResponse> findRandomClans(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        String randomUuid = java.util.UUID.randomUUID().toString();
        List<String> ids = clanRepository.findRandomIds(randomUuid, PageRequest.of(0, limit));
        if (ids.size() < limit) {
            int remaining = limit - ids.size();
            List<String> wrapAroundIds = clanRepository.findRandomIds("0", PageRequest.of(0, remaining));
            java.util.List<String> modifiableIds = new java.util.ArrayList<>(ids);
            for (String id : wrapAroundIds) {
                if (!modifiableIds.contains(id)) {
                    modifiableIds.add(id);
                }
            }
            ids = modifiableIds;
        }

        if (ids.isEmpty()) {
            return List.of();
        }

        var rows = new java.util.ArrayList<>(clanRepository.findClanSummariesByIds(ids));
        java.util.Collections.shuffle(rows);
        return mapToSummaryResponses(rows);
    }

    @Override
    @Transactional(readOnly = true)
    public ClanDetailResponse getClanDetail(String clanId) {
        clanValidator.requireClanId(clanId);
        final String validClanId = Objects.requireNonNull(clanId);
        Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(
                        SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        List<ClanMemberDTO> memberDTOs = memberRepository.getClanMembersByClanId(validClanId).stream()
                .map(socialMapper::toClanMemberDTO)
                .toList();

        int rank = (int) clanRepository.findRankByTierAndScore(clan.getTier(), clan.getScore(), clan.getId());

        ModifierSummary mod = modifierService.getModifierSummary(validClanId);

        return socialMapper.toClanDetailResponse(clan, rank, memberDTOs.size(), memberDTOs, mod.buffs(),
                mod.debuffs());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MyClanResponse> getMyClanByUsername(final String username) {
        return memberRepository.findByUsername(username)
                .map(ClanMember::getClanId)
                .filter(id -> id != null && !id.isBlank())
                .flatMap(clanRepository::findById)
                .map(clan -> buildMyClanResponse(clan, username));
    }

    private MyClanResponse buildMyClanResponse(Clan clan, String username) {
        List<ClanMember> members = memberRepository.getClanMembersByClanId(clan.getId()).stream().toList();
        int rank = (int) clanRepository.findRankByTierAndScore(clan.getTier(), clan.getScore(), clan.getId());

        String role = clan.getLeaderUsername().equals(username)
                ? SocialConstants.MY_CLAN_ROLE_LEADER
                : SocialConstants.MY_CLAN_ROLE_MEMBER;

        return socialMapper.toMyClanResponse(clan, role, rank, members);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaderboardResponse> getLeaderboardByTier(String username, String search) {
        final Optional<Clan> userClan = Optional.ofNullable(username)
                .flatMap(memberRepository::findByUsername)
                .map(ClanMember::getClanId)
                .flatMap(clanRepository::findById);

        return Stream.of(Tier.values())
                .map(tier -> buildLeaderboardForTier(tier, search, userClan))
                .toList();
    }

    private LeaderboardResponse buildLeaderboardForTier(Tier tier, String search, Optional<Clan> userClan) {
        PageRequest pageRequest = PageRequest.of(0, SocialConstants.LEADERBOARD_LIMIT);

        List<ClanLeaderboardRow> rows = (search == null || search.isBlank())
                ? clanRepository.findLeaderboardByTier(tier, pageRequest)
                : clanRepository.findLeaderboardByTierAndName(tier, search, pageRequest);

        List<LeaderboardEntryResponse> rankedEntries = IntStream.range(0, rows.size())
                .mapToObj(i -> socialMapper.toLeaderboardEntryResponse(rows.get(i), i + 1))
                .toList();

        LeaderboardEntryResponse userEntry = userClan
                .filter(clan -> clan.getTier() == tier)
                .map(clan -> {
                    int userRank = (int) clanRepository.findRankByTierAndScore(tier,
                            clan.getScore(), clan.getId());
                    int memberCount = (int) memberRepository.countByClanId(clan.getId());
                    return socialMapper.toLeaderboardEntryResponse(clan, userRank, memberCount);
                })
                .orElse(null);

        return new LeaderboardResponse(tier.getDisplayName(), rankedEntries, userEntry);
    }
}
