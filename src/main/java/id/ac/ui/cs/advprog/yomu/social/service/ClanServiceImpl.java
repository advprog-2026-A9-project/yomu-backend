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
import id.ac.ui.cs.advprog.yomu.social.dto.ClanLeaderboardRow;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardEntryResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.MyClanResponse;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanRepository;
import id.ac.ui.cs.advprog.yomu.social.strategy.ScoringStrategyFactory;
import id.ac.ui.cs.advprog.yomu.social.validation.ClanValidation;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClanServiceImpl implements ClanService {

    private final ClanRepository clanRepository;
    private final ClanMemberRepository memberRepository;
    private final ScoringStrategyFactory scoringStrategyFactory;
    private final ClanModifierService modifierService;
    private final ClanValidation clanValidation;

    @Override
    @Transactional
    public Clan createClan(final ClanRequest request) {
        clanValidation.requireClanNameAvailable(clanRepository.existsByName(request.getName()));

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
        clanValidation.requireClanId(clanId);
        clanValidation.requireUserId(userId);
        final String validClanId = Objects.requireNonNull(clanId);
        final String validUserId = Objects.requireNonNull(userId);

        Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        clanValidation.requireLeaderPrivilege(clan, validUserId, "Permission to edit clan information denied");

        clanRepository.findByName(request.getName())
                .filter(existingClan -> !existingClan.getId().equals(validClanId))
                .ifPresent(existingClan -> clanValidation.requireClanNameAvailable(true));

        clan.setName(request.getName());
        clan.setDescription(request.getDescription());

        return clanRepository.save(clan);
    }

    @Override
    @Transactional
    public void joinClan(final String clanId, final String userId, final String username, final String role) {
        clanValidation.requireClanId(clanId);
        final String validClanId = Objects.requireNonNull(clanId);
        final String validUserId = Objects.requireNonNull(userId);

        clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        clanValidation.requireNotAlreadyMember(memberRepository.findByClanIdAndUserId(clanId, userId).isPresent());
        clanValidation.requireNotMemberOfOtherClan(memberRepository.findByUserId(userId).isPresent());
        clanValidation.requireClanNotFull(memberRepository.countByClanId(validClanId));

        final ClanMember member = new ClanMember();
        member.setUsername(username);
        member.setClanId(validClanId);
        member.setUserId(validUserId);
        member.setRole(role);
        memberRepository.save(member);
    }

    @Override
    public List<ClanMember> getMembersByClanId(final String clanId) {
        clanValidation.requireClanId(clanId);

        return memberRepository.getClanMembersByClanId(clanId).stream().toList();
    }

    @Override
    public List<id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryResponse> findAll() {
        return clanRepository.findAllClanSummaries().stream()
                .map(row -> new id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryResponse(
                        row.getClanId(),
                        row.getClanName(),
                        row.getDescription(),
                        row.getLeaderUserId(),
                        row.getTier().name(),
                        row.getScore(),
                        row.getMemberCount()))
                .toList();
    }

    @Override
    public id.ac.ui.cs.advprog.yomu.social.dto.ClanDetailResponse getClanDetail(String clanId) {
        clanValidation.requireClanId(clanId);
        final String validClanId = Objects.requireNonNull(clanId);
        Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        List<ClanMember> members = memberRepository.getClanMembersByClanId(validClanId).stream().toList();
        List<id.ac.ui.cs.advprog.yomu.social.dto.ClanMemberDTO> memberDTOs = members.stream()
                .map(m -> new id.ac.ui.cs.advprog.yomu.social.dto.ClanMemberDTO(
                        m.getUserId(), m.getUsername(), m.getRole(), 0, 0, true))
                .toList();

        double avgAccuracy = 0.0;
        int rank = (int) clanRepository.findRankByTierAndScore(clan.getTier(), clan.getScore(), clan.getId());

        List<id.ac.ui.cs.advprog.yomu.social.dto.ClanModifierDTO> activeBuffs = new ArrayList<>();
        List<id.ac.ui.cs.advprog.yomu.social.dto.ClanModifierDTO> debuffs = new ArrayList<>();

        int maxMembers = SocialConstants.MAX_CLAN_SIZE;

        return new id.ac.ui.cs.advprog.yomu.social.dto.ClanDetailResponse(
                clan.getId(),
                clan.getName(),
                clan.getDescription() == null ? "" : clan.getDescription(),
                clan.getLeaderUserId(),
                clan.getTier().name(),
                rank,
                clan.getScore(),
                members.size(),
                maxMembers,
                avgAccuracy,
                memberDTOs,
                activeBuffs,
                debuffs);
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
                            .map(clan -> toMyClanResponse(clan, userId));
                });
    }

    @Override
    @Transactional
    public void deleteClan(final String clanId, final String leaderId) {
        clanValidation.requireClanId(clanId);
        clanValidation.requireUserId(leaderId);
        final String validClanId = Objects.requireNonNull(clanId);
        final String validLeaderId = Objects.requireNonNull(leaderId);

        final Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        clanValidation.requireLeaderPrivilege(clan, validLeaderId, "You have no permission to delete this clan.");

        memberRepository.deleteByClanId(validClanId);

        clanRepository.delete(Objects.requireNonNull(clan));
    }

    @Override
    @Transactional
    public void leaveClan(final String clanId, final String userId) {
        clanValidation.requireClanId(clanId);
        clanValidation.requireUserId(userId);
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
            final String newLeaderId = clanValidation.resolveReplacementLeader(allMembers, leaderId);

            clan.setLeaderUserId(newLeaderId);
            clanRepository.save(clan);
            memberRepository.deleteByClanIdAndUserId(clan.getId(), leaderId);
        }
    }

    private MyClanResponse toMyClanResponse(final Clan clan, final String currentUserId) {
        String role = clan.getLeaderUserId().equals(currentUserId)
                ? SocialConstants.MY_CLAN_ROLE_LEADER
                : SocialConstants.MY_CLAN_ROLE_MEMBER;
        List<ClanMember> members = memberRepository.getClanMembersByClanId(clan.getId()).stream().toList();

        int rank = (int) clanRepository.findRankByTierAndScore(clan.getTier(), clan.getScore(), clan.getId());

        return new MyClanResponse(
                clan.getId(),
                clan.getName(),
                clan.getDescription(),
                clan.getLeaderUserId(),
                role,
                clan.getTier().getDisplayName(),
                clan.getScore(),
                rank,
                members);
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
                        ClanLeaderboardRow row = rows.get(i);
                        rankedEntries.add(new LeaderboardEntryResponse(
                                row.getClanId(),
                                row.getClanName(),
                                row.getTier().getDisplayName(),
                                row.getScore(),
                                i + 1,
                                Math.toIntExact(row.getMemberCount())));
                    }

                    LeaderboardEntryResponse userEntry = null;
                    if (userClan.isPresent() && userClan.get().getTier() == tier) {
                        Clan uc = userClan.get();
                        int userRank = (int) clanRepository.findRankByTierAndScore(tier, uc.getScore(), uc.getId());
                        userEntry = new LeaderboardEntryResponse(
                                uc.getId(),
                                uc.getName(),
                                tier.getDisplayName(),
                                uc.getScore(),
                                userRank,
                                (int) memberRepository.countByClanId(uc.getId()));
                    }

                    return new LeaderboardResponse(tier.getDisplayName(), rankedEntries, userEntry);
                })
                .toList();
    }

    @Override
    @Transactional
    public void updateClanScore(String clanId, int basePoints) {
        clanValidation.requireClanId(clanId);
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
        clanValidation.requireClanId(clanId);
        clanValidation.requireUserId(leaderId);
        clanValidation.requireUserId(memberId);

        final String validClanId = Objects.requireNonNull(clanId);

        Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        clanValidation.requireLeaderPrivilege(clan, leaderId, "Hanya Leader yang bisa mengeluarkan anggota");

        if (leaderId.equals(memberId)) {
            throw new IllegalArgumentException("Leader tidak bisa mengeluarkan diri sendiri");
        }

        memberRepository.deleteByClanIdAndUserId(clanId, memberId);
    }
}
