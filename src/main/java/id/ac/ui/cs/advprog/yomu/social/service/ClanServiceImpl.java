package id.ac.ui.cs.advprog.yomu.social.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

<<<<<<< HEAD
=======
import id.ac.ui.cs.advprog.yomu.auth.model.User;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
>>>>>>> 89eb63e (feat(social-clan): update security and clan access)
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (clanRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Nama Clan sudah digunakan");
        }

        final Clan clan = new Clan();
        clan.setName(request.getName());
        clan.setDescription(request.getDescription());
        clan.setLeaderUserId(request.getUserId());

        final Clan savedClan = clanRepository.save(clan);

        joinClan(savedClan.getId(), request.getUserId(), request.getUsername(), "LEADER");

        return savedClan;
    }

    @Override
    @Transactional
<<<<<<< HEAD
    public void joinClan(final String clanId, final String userId) {
=======
    public void joinClan(final String clanId, final String userId, final String username, final String role) {
        if (clanId == null) {
            throw new IllegalStateException("Class ID is null");
        }

>>>>>>> 89eb63e (feat(social-clan): update security and clan access)
        clanRepository.findById(clanId)
                .orElseThrow(() -> new IllegalArgumentException("Clan tidak ditemukan"));

        memberRepository.findByClanIdAndUserId(clanId, userId)
                .ifPresent(existing -> {
                    throw new IllegalStateException("Kamu sudah menjadi anggota Clan ini");
                });

        memberRepository.findByUserId(userId)
                .ifPresent(existing -> {
                    throw new IllegalStateException("Kamu sudah tergabung di Clan lain");
                });

        final ClanMember member = new ClanMember();
        member.setUsername(username);
        member.setClanId(clanId);
        member.setUserId(userId);
        member.setRole(role);
        memberRepository.save(member);
    }

    public List<ClanMember> getMembersByClanId(final String clanId) {
        if (clanId == null) {
            throw new IllegalStateException("Clan ID is null");
        }

        return memberRepository.getClanMembersByClanId(clanId).stream().toList();
    }

    @Override
    public List<Clan> findAll() {
        return clanRepository.findAll();
    }

    @Override
    public Optional<MyClanResponse> getMyClanByUserId(final String userId) {
        return memberRepository.findByUserId(userId)
                .flatMap(member -> clanRepository.findById(member.getClanId())
                        .map(clan -> toMyClanResponse(clan, userId)));
    }

    @Override
    @Transactional
    public void deleteClan(final String clanId, final String leaderId) {
        final Clan clan = clanRepository.findById(clanId)
                .orElseThrow(() -> new IllegalArgumentException("Clan tidak ditemukan"));

        if (!clan.getLeaderUserId().equals(leaderId)) {
            throw new IllegalStateException("Hanya Leader yang bisa menghapus Clan");
        }

        memberRepository.deleteByClanId(clanId);

        clanRepository.delete(clan);
    }

    @Override
    @Transactional
    public void leaveClan(final String clanId, final String userId) {
        final Clan clan = clanRepository.findById(clanId)
                .orElseThrow(() -> new IllegalArgumentException("Clan tidak ditemukan"));

        if (clan.getLeaderUserId().equals(userId)) {
            handleLeaderLeave(clan, userId);
        } else {
            memberRepository.deleteByClanIdAndUserId(clanId, userId);
        }
    }

    private void handleLeaderLeave(final Clan clan, final String leaderId) {
        final List<ClanMember> allMembers = memberRepository.findByClanId(clan.getId());
        int MIN_CLAN_SIZE = 1;

        if (allMembers.size() <= MIN_CLAN_SIZE) {
            memberRepository.deleteByClanIdAndUserId(clan.getId(), leaderId);
            clanRepository.delete(clan);
        }

        else {
            final String newLeaderId = allMembers.stream()
                    .map(ClanMember::getUserId)
                    .filter(id -> !id.equals(leaderId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Gagal menemukan pengganti Leader"));

            clan.setLeaderUserId(newLeaderId);
            clanRepository.save(clan);
            memberRepository.deleteByClanIdAndUserId(clan.getId(), leaderId);
        }
    }

    private MyClanResponse toMyClanResponse(final Clan clan, final String currentUserId) {
        String role = clan.getLeaderUserId().equals(currentUserId) ? "KETUA" : "ANGGOTA";
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

        return List.of(Tier.values()).stream()
                .map(tier -> {
                    List<LeaderboardEntryResponse> entries = allClans.stream()
                            .filter(clan -> clan.getTier() != null && clan.getTier() == tier)
                            .sorted(Comparator.comparingInt(Clan::getScore).reversed())
                            .limit(100)
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
        Clan clan = clanRepository.findById(clanId)
                .orElseThrow(() -> new IllegalArgumentException("Clan tidak ditemukan"));

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
            int promoteCount = Math.max(1, (int) Math.ceil(totalClans * 0.2));
            int demoteCount = Math.max(1, (int) Math.ceil(totalClans * 0.2));

            // Promote top clans
            for (int i = 0; i < Math.min(promoteCount, totalClans); i++) {
                String clanId = tierBoard.entries().get(i).clanId();
                Clan clan = clanRepository.findById(clanId).orElse(null);
                if (clan != null && clan.getTier() != Tier.DIAMOND) {
                    clan.setTier(clan.getTier().promote());
                    clan.setScore(0); // Reset score for new season
                    clanRepository.save(clan);
                }
            }

            // Demote bottom clans
            for (int i = Math.max(0, totalClans - demoteCount); i < totalClans; i++) {
                String clanId = tierBoard.entries().get(i).clanId();
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