package id.ac.ui.cs.advprog.yomu.social.service.clan.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanDetailResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanLeaderboardRow;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanMemberDTO;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanModifierDTO;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardEntryResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.MyClanResponse;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.model.ClanModifier;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanModifierRepository;
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
    private final ClanModifierRepository modifierRepository;
    private final ClanValidator clanValidator;
    private final SocialMapper socialMapper;
    private final ClanModifierService modifierService;

    private record ActiveModifiers(List<ClanModifierDTO> buffs, List<ClanModifierDTO> debuffs) {}

    private ActiveModifiers resolveActiveModifiers(String clanId) {
        List<ClanModifier> all = modifierRepository.findByClanIdAndActiveTrue(clanId);
        List<ClanModifierDTO> buffs = all.stream()
                .filter(ClanModifier::isBuff)
                .map(socialMapper::toClanModifierDTO)
                .toList();
        List<ClanModifierDTO> debuffs = all.stream()
                .filter(ClanModifier::isDebuff)
                .map(socialMapper::toClanModifierDTO)
                .toList();
        return new ActiveModifiers(buffs, debuffs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClanSummaryResponse> findAll(String search) {
        var rows = (search == null || search.isBlank())
                ? clanRepository.findAllClanSummaries()
                : clanRepository.findClanSummariesByQuery(search);

        return rows.stream()
                .map(row -> {
                    ActiveModifiers modifiers = resolveActiveModifiers(row.getClanId());
                    int effectiveScore = (int) Math
                            .round(row.getScore() * modifierService
                                    .getActiveMultiplier(row.getClanId()));

                    return socialMapper.toClanSummaryResponse(row, modifiers.buffs(), modifiers.debuffs(),
                            effectiveScore);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClanSummaryResponse> findRandomClans(int limit) {
        var rows = clanRepository.findRandomClanSummaries(limit);

        return rows.stream()
                .map(row -> {
                    ActiveModifiers modifiers = resolveActiveModifiers(row.getClanId());
                    int effectiveScore = (int) Math
                            .round(row.getScore() * modifierService
                                    .getActiveMultiplier(row.getClanId()));

                    return socialMapper.toClanSummaryResponse(row, modifiers.buffs(), modifiers.debuffs(),
                            effectiveScore);
                })
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

        List<ClanMember> members = memberRepository.getClanMembersByClanId(validClanId).stream().toList();
        List<ClanMemberDTO> memberDTOs = members.stream()
                .map(socialMapper::toClanMemberDTO)
                .toList();

        int rank = (int) clanRepository.findRankByTierAndScore(clan.getTier(), clan.getScore(), clan.getId());

        ActiveModifiers modifiers = resolveActiveModifiers(validClanId);

        return socialMapper.toClanDetailResponse(clan, rank, members.size(), memberDTOs, modifiers.buffs(), modifiers.debuffs());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MyClanResponse> getMyClanByUsername(final String username) {
        return memberRepository.findByUsername(username)
                .flatMap(member -> {
                    final String clanId = member.getClanId();
                    if (clanId == null || clanId.isBlank()) {
                        return Optional.empty();
                    }

                    final Clan clan = clanRepository.findById(clanId).orElse(null);
                    if (clan == null) {
                        return Optional.empty();
                    }

                    final List<ClanMember> members = memberRepository.getClanMembersByClanId(clanId).stream().toList();
                    final int rank = (int) clanRepository.findRankByTierAndScore(clan.getTier(), clan.getScore(),
                            clan.getId());

                    final String role = clan.getLeaderUsername().equals(username)
                            ? SocialConstants.MY_CLAN_ROLE_LEADER
                            : SocialConstants.MY_CLAN_ROLE_MEMBER;

                    final MyClanResponse response = socialMapper.toMyClanResponse(clan, role, rank,
                            members);
                    return Optional.of(response);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaderboardResponse> getLeaderboardByTier(String username, String search) {
        final Optional<ClanMember> userMember = username != null
                ? memberRepository.findByUsername(username)
                : Optional.empty();

        final Optional<Clan> userClan = userMember
                .map(ClanMember::getClanId)
                .flatMap(clanRepository::findById);

        return Stream.of(Tier.values())
                .map(tier -> {
                    List<ClanLeaderboardRow> rows = (search == null || search.isBlank())
                            ? clanRepository.findLeaderboardByTier(
                                    tier,
                                    PageRequest.of(0,
                                            SocialConstants.LEADERBOARD_LIMIT))
                            : clanRepository.findLeaderboardByTierAndName(
                                    tier,
                                    search,
                                    PageRequest.of(0,
                                            SocialConstants.LEADERBOARD_LIMIT));

                    List<LeaderboardEntryResponse> rankedEntries = new ArrayList<>();
                    for (int i = 0; i < rows.size(); i++) {
                        rankedEntries.add(socialMapper.toLeaderboardEntryResponse(rows.get(i), i + 1));
                    }

                    LeaderboardEntryResponse userEntry = null;
                    if (userClan.isPresent() && userClan.get().getTier() == tier) {
                        Clan uc = userClan.get();
                        int userRank = (int) clanRepository.findRankByTierAndScore(tier, uc.getScore(), uc.getId());
                        userEntry = socialMapper.toLeaderboardEntryResponse(uc, userRank,
                                (int) memberRepository.countByClanId(uc.getId()));
                    }

                    return new LeaderboardResponse(tier.getDisplayName(), rankedEntries, userEntry);
                })
                .toList();
    }
}
