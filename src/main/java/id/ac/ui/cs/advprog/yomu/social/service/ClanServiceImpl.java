package id.ac.ui.cs.advprog.yomu.social.service;

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
import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
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
import id.ac.ui.cs.advprog.yomu.social.strategy.ScoringStrategyResolver;
import id.ac.ui.cs.advprog.yomu.social.validation.ClanValidator;
import id.ac.ui.cs.advprog.yomu.social.mapper.SocialMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClanServiceImpl implements ClanService {

    private final ClanRepository clanRepository;
    private final ClanMemberRepository memberRepository;
    private final ClanModifierRepository modifierRepository;
    private final ScoringStrategyResolver scoringStrategyFactory;
    private final ClanModifierService modifierService;
    private final ClanValidator clanValidator;
    private final SocialMapper socialMapper;

    @Override
    @Transactional
    public Clan createClan(final ClanRequest request) {
        clanValidator.requireClanNameAvailable(clanRepository.existsByName(request.getName()));

        final Clan clan = new Clan();
        clan.setName(request.getName());
        clan.setDescription(request.getDescription());
        clan.setLeaderUserId(request.getUserId());

        final Clan savedClan = clanRepository.save(clan);

        joinClan(savedClan.getId(), request.getUserId(), request.getUsername(), SocialConstants.ROLE_LEADER);

        return savedClan;
    }

    @Override
    @Transactional
    public Clan editClan(final String clanId, final String userId, final ClanRequest request) {
        clanValidator.requireClanId(clanId);
        clanValidator.requireUserId(userId);
        final String validClanId = Objects.requireNonNull(clanId);
        final String validUserId = Objects.requireNonNull(userId);

        Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        clanValidator.requireLeaderPrivilege(clan, validUserId, "Permission to edit clan information denied");

        clanRepository.findByName(request.getName())
                .filter(existingClan -> !existingClan.getId().equals(validClanId))
                .ifPresent(existingClan -> clanValidator.requireClanNameAvailable(true));

        clan.setName(request.getName());
        clan.setDescription(request.getDescription());

        return clanRepository.save(clan);
    }

    @Override
    @Transactional
    public void joinClan(final String clanId, final String userId, final String username, final String role) {
        clanValidator.requireClanId(clanId);
        final String validClanId = Objects.requireNonNull(clanId);
        final String validUserId = Objects.requireNonNull(userId);

        clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        clanValidator.requireNotAlreadyMember(memberRepository.findByClanIdAndUserId(clanId, userId).isPresent());
        clanValidator.requireNotMemberOfOtherClan(memberRepository.findByUserId(userId).isPresent());
        clanValidator.requireClanNotFull(memberRepository.countByClanId(validClanId));

        final ClanMember member = new ClanMember();
        member.setUsername(username);
        member.setClanId(validClanId);
        member.setUserId(validUserId);
        member.setRole(role);
        memberRepository.save(member);
    }

    @Override
    public List<ClanMember> getMembersByClanId(final String clanId) {
        clanValidator.requireClanId(clanId);

        return memberRepository.getClanMembersByClanId(clanId).stream().toList();
    }

    @Override
    public List<ClanSummaryResponse> findAll() {
        return clanRepository.findAllClanSummaries().stream()
                .map(row -> {
                    List<ClanModifier> activeModifiers = modifierRepository.findByClanIdAndActiveTrue(row.getClanId());
                    List<ClanModifierDTO> activeBuffs = activeModifiers.stream()
                            .filter(modifier -> modifier.getMultiplier() >= 1.0d)
                            .map(socialMapper::toClanModifierDTO)
                            .toList();
                    List<ClanModifierDTO> debuffs = activeModifiers.stream()
                            .filter(modifier -> modifier.getMultiplier() < 1.0d)
                            .map(socialMapper::toClanModifierDTO)
                            .toList();

                    int effectiveScore = (int) Math
                            .round(row.getScore() * modifierService.getActiveMultiplier(row.getClanId()));

                    return socialMapper.toClanSummaryResponse(row, activeBuffs, debuffs, effectiveScore);
                })
                .toList();
    }

    @Override
    public ClanDetailResponse getClanDetail(String clanId) {
        clanValidator.requireClanId(clanId);
        final String validClanId = Objects.requireNonNull(clanId);
        Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        List<ClanMember> members = memberRepository.getClanMembersByClanId(validClanId).stream().toList();
        List<ClanMemberDTO> memberDTOs = members.stream()
                .map(socialMapper::toClanMemberDTO)
                .toList();

        int rank = (int) clanRepository.findRankByTierAndScore(clan.getTier(), clan.getScore(), clan.getId());

        List<ClanModifier> activeModifiers = modifierRepository.findByClanIdAndActiveTrue(validClanId);
        List<id.ac.ui.cs.advprog.yomu.social.dto.ClanModifierDTO> activeBuffs = activeModifiers.stream()
                .filter(modifier -> modifier.getMultiplier() >= 1.0d)
                .map(socialMapper::toClanModifierDTO)
                .toList();
        List<id.ac.ui.cs.advprog.yomu.social.dto.ClanModifierDTO> debuffs = activeModifiers.stream()
                .filter(modifier -> modifier.getMultiplier() < 1.0d)
                .map(socialMapper::toClanModifierDTO)
                .toList();

        return socialMapper.toClanDetailResponse(clan, rank, members.size(), memberDTOs, activeBuffs, debuffs);
    }

    @Override
    public Optional<MyClanResponse> getMyClanByUserId(final String userId) {
        return memberRepository.findByUserId(userId)
                .flatMap(member -> {
                    final String clanId = member.getClanId();
                    if (clanId == null) {
                        return Optional.empty();
                    }

                    return clanRepository.findById(clanId)
                            .map(clan -> {
                                String role = clan.getLeaderUserId().equals(userId)
                                        ? SocialConstants.MY_CLAN_ROLE_LEADER
                                        : SocialConstants.MY_CLAN_ROLE_MEMBER;
                                List<ClanMember> members = memberRepository.getClanMembersByClanId(clan.getId())
                                        .stream().toList();
                                int rank = (int) clanRepository.findRankByTierAndScore(clan.getTier(), clan.getScore(),
                                        clan.getId());

                                return socialMapper.toMyClanResponse(clan, role, rank, members);
                            });
                });
    }

    @Override
    @Transactional
    public void deleteClan(final String clanId, final String leaderId) {
        clanValidator.requireClanId(clanId);
        clanValidator.requireUserId(leaderId);
        final String validClanId = Objects.requireNonNull(clanId);
        final String validLeaderId = Objects.requireNonNull(leaderId);

        final Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        clanValidator.requireLeaderPrivilege(clan, validLeaderId, "You have no permission to delete this clan.");

        memberRepository.deleteByClanId(validClanId);

        clanRepository.delete(Objects.requireNonNull(clan));
    }

    @Override
    @Transactional
    public void leaveClan(final String clanId, final String userId) {
        clanValidator.requireClanId(clanId);
        clanValidator.requireUserId(userId);
        final String validClanId = Objects.requireNonNull(clanId);
        final String validUserId = Objects.requireNonNull(userId);

        final Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        if (clan.getLeaderUserId().equals(validUserId)) {
            handleLeaderLeave(clan, validUserId);
        } else {
            memberRepository.deleteByClanIdAndUserId(validClanId, validUserId);
        }
    }

    private void handleLeaderLeave(final Clan clan, final String leaderId) {
        final List<ClanMember> allMembers = memberRepository.findByClanId(clan.getId());
        int minClanSize = SocialConstants.MIN_CLAN_SIZE;

        if (allMembers.size() <= minClanSize) {
            memberRepository.deleteByClanIdAndUserId(clan.getId(), leaderId);
            clanRepository.delete(Objects.requireNonNull(clan));
        }

        else {
            final String newLeaderId = clanValidator.resolveReplacementLeader(allMembers, leaderId);

            clan.setLeaderUserId(newLeaderId);
            clanRepository.save(clan);
            memberRepository.deleteByClanIdAndUserId(clan.getId(), leaderId);
        }
    }

    @Override
    public List<LeaderboardResponse> getLeaderboardByTier(String userId) {
        final Optional<ClanMember> userMember = userId != null
                ? memberRepository.findByUserId(userId)
                : Optional.empty();

        final Optional<Clan> userClan = userMember
                .map(ClanMember::getClanId)
                .flatMap(clanRepository::findById);

        return Stream.of(Tier.values())
                .map(tier -> {
                    List<ClanLeaderboardRow> rows = clanRepository.findLeaderboardByTier(
                            tier,
                            PageRequest.of(0, SocialConstants.LEADERBOARD_LIMIT));

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

    @Override
    @Transactional
    public void updateClanScore(String clanId, int basePoints) {
        clanValidator.requireClanId(clanId);
        final String validClanId = Objects.requireNonNull(clanId);

        Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        var strategy = scoringStrategyFactory.getStrategy(clan.getTier());
        int baseIncrement = strategy.calculateScore(clan, basePoints);
        double multiplier = modifierService.getActiveMultiplier(validClanId);
        int calculatedIncrement = (int) Math.round(baseIncrement * multiplier);

        clan.setScore(clan.getScore() + calculatedIncrement);
        clanRepository.save(clan);
    }

    @Override
    @Transactional
    public void kickMember(String clanId, String leaderId, String memberId) {
        clanValidator.requireClanId(clanId);
        clanValidator.requireUserId(leaderId);
        clanValidator.requireUserId(memberId);

        final String validClanId = Objects.requireNonNull(clanId);

        Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        clanValidator.requireLeaderPrivilege(clan, leaderId, "Hanya Leader yang bisa mengeluarkan anggota");

        if (leaderId.equals(memberId)) {
            throw new IllegalArgumentException("Leader tidak bisa mengeluarkan diri sendiri");
        }

        memberRepository.deleteByClanIdAndUserId(clanId, memberId);
    }
}
