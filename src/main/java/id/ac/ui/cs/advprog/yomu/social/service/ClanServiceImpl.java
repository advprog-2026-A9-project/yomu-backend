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

        clanValidation.requireClanNameAvailable(clanRepository.existsByName(request.getName()));

        Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        clanValidation.requireLeaderPrivilege(clan, validUserId, "Permission to edit clan information denied");

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
    public List<Clan> findAll() {
        return clanRepository.findAll();
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

        return new MyClanResponse(
                clan.getId(),
                clan.getName(),
                clan.getDescription(),
                clan.getLeaderUserId(),
                role,
                members);
    }

    @Override
    public List<LeaderboardResponse> getLeaderboardByTier() {
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

                    return new LeaderboardResponse(tier.getDisplayName(), rankedEntries);
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

}