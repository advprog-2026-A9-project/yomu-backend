package id.ac.ui.cs.advprog.yomu.social.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
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

    @Override
    @Transactional
    public Clan createClan(final ClanRequest request) {
        ClanValidation.requireClanNameAvailable(clanRepository.existsByName(request.getName()));

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
        ClanValidation.requireClanId(clanId);
        ClanValidation.requireUserId(userId);
        final String validClanId = Objects.requireNonNull(clanId);
        final String validUserId = Objects.requireNonNull(userId);

        ClanValidation.requireClanNameAvailable(clanRepository.existsByName(request.getName()));

        Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        ClanValidation.requireLeaderPrivilege(clan, validUserId, "Permission to edit clan information denied");
        
        clan.setName(request.getName());
        clan.setDescription(request.getDescription());

        return clanRepository.save(clan);
    }

    @Override
    @Transactional
    public void joinClan(final String clanId, final String userId, final String username, final String role) {
        ClanValidation.requireClanId(clanId);
        final String validClanId = Objects.requireNonNull(clanId);
        final String validUserId = Objects.requireNonNull(userId);

        clanRepository.findById(validClanId)
            .orElseThrow(() -> new IllegalArgumentException(SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        ClanValidation.requireNotAlreadyMember(memberRepository.findByClanIdAndUserId(clanId, userId).isPresent());
        ClanValidation.requireNotMemberOfOtherClan(memberRepository.findByUserId(userId).isPresent());

        final ClanMember member = new ClanMember();
        member.setUsername(username);
        member.setClanId(validClanId);
        member.setUserId(validUserId);
        member.setRole(role);
        memberRepository.save(member);
    }

    @Override
    public List<ClanMember> getMembersByClanId(final String clanId) {
        ClanValidation.requireClanId(clanId);

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
        ClanValidation.requireClanId(clanId);
        ClanValidation.requireUserId(leaderId);
        final String validClanId = Objects.requireNonNull(clanId);
        final String validLeaderId = Objects.requireNonNull(leaderId);

        final Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        ClanValidation.requireLeaderPrivilege(clan, validLeaderId, "You have no permission to delete this clan.");

        memberRepository.deleteByClanId(validClanId);

        clanRepository.delete(Objects.requireNonNull(clan));
    }

    @Override
    @Transactional
    public void leaveClan(final String clanId, final String userId) {
        ClanValidation.requireClanId(clanId);
        ClanValidation.requireUserId(userId);
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
            final String newLeaderId = ClanValidation.resolveReplacementLeader(allMembers, leaderId);

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
        List<Clan> allClans = clanRepository.findAll();

        return Stream.of(Tier.values())
                .map(tier -> {
                    List<LeaderboardEntryResponse> entries = allClans.stream()
                            .filter(clan -> clan.getTier() != null && clan.getTier() == tier)
                            .sorted(Comparator.comparingInt(Clan::getScore).reversed())
                            .limit(SocialConstants.LEADERBOARD_LIMIT)
                            .map(clan -> {
                                int memberCount = Math.toIntExact(memberRepository.countByClanId(clan.getId()));
                                return new LeaderboardEntryResponse(
                                        clan.getId(),
                                        clan.getName(),
                                        clan.getTier().getDisplayName(),
                                        clan.getScore(),
                                        0,
                                        memberCount);
                            })
                            .toList();

                    List<LeaderboardEntryResponse> rankedEntries = new java.util.ArrayList<>();
                    for (int i = 0; i < entries.size(); i++) {
                        LeaderboardEntryResponse e = entries.get(i);
                        rankedEntries.add(new LeaderboardEntryResponse(
                                e.clanId(), e.clanName(), e.tier(), e.score(), i + 1, e.memberCount()));
                    }

                    return new LeaderboardResponse(tier.getDisplayName(), rankedEntries);
                })
                .toList();
    }

    @Override
    @Transactional
    public void updateClanScore(String clanId, int basePoints) {
        ClanValidation.requireClanId(clanId);
        final String validClanId = Objects.requireNonNull(clanId);

        Clan clan = clanRepository.findById(validClanId)
            .orElseThrow(() -> new IllegalArgumentException(SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        var strategy = scoringStrategyFactory.getStrategy(clan.getTier());
        int calculatedScore = strategy.calculateScore(clan, basePoints);

        clan.setScore(calculatedScore);
        clanRepository.save(clan);
    }

    @Override
    @Transactional
    public void endSeason() {
        List<LeaderboardResponse> leaderboard = getLeaderboardByTier();

        // Promote top 20% and demote bottom 20% of each tier
        for (LeaderboardResponse tierBoard : leaderboard) {
            if (tierBoard.entries().isEmpty())
                continue;

            int totalClans = tierBoard.entries().size();
            int promoteCount = Math.max(1, (int) Math.ceil(totalClans * SocialConstants.SEASON_CHANGE_RATIO));
            int demoteCount = Math.max(1, (int) Math.ceil(totalClans * SocialConstants.SEASON_CHANGE_RATIO));

            // Promote top clans
            for (int i = 0; i < Math.min(promoteCount, totalClans); i++) {
                final String clanId = Objects.requireNonNull(tierBoard.entries().get(i).clanId());
                Clan clan = clanRepository.findById(clanId).orElse(null);
                if (clan != null && clan.getTier() != Tier.DIAMOND) {
                    clan.setTier(clan.getTier().promote());
                    clan.setScore(0); // Reset score for new season
                    clanRepository.save(clan);
                }
            }

            // Demote bottom clans
            for (int i = Math.max(0, totalClans - demoteCount); i < totalClans; i++) {
                final String clanId = Objects.requireNonNull(tierBoard.entries().get(i).clanId());
                Clan clan = clanRepository.findById(clanId).orElse(null);
                if (clan != null && clan.getTier() != Tier.BRONZE) {
                    clan.setTier(clan.getTier().demote());
                    clan.setScore(0); // Reset score for new season
                    clanRepository.save(clan);
                }
            }
        }

        // Reset scores for all clans not promoted/demoted
        List<Clan> allClans = clanRepository.findAll();
        allClans.forEach(clan -> {
            if (clan.getScore() > 0) {
                clan.setScore(0);
                clanRepository.save(clan);
            }
        });
    }
}