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

    private ClanSummaryResponse mapToSummaryResponse(ClanSummaryRow row) {
        ModifierSummary mod = modifierService.getModifierSummary(row.getClanId());
        int effectiveScore = (int) Math.round(row.getScore() * mod.multiplier());
        return socialMapper.toClanSummaryResponse(row, mod.buffs(), mod.debuffs(), effectiveScore);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClanSummaryResponse> findAll(String search) {
        var rows = (search == null || search.isBlank())
                ? clanRepository.findAllClanSummaries()
                : clanRepository.findClanSummariesByQuery(search);

        return rows.stream()
                .map(this::mapToSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClanSummaryResponse> findRandomClans(int limit) {
        return clanRepository.findRandomClanSummaries(limit).stream()
                .map(this::mapToSummaryResponse)
                .toList();
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
