package id.ac.ui.cs.advprog.yomu.social.service.clan.membership;

import java.util.List;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.event.ClanShouldBeDeletedEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserJoinClanEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserLeaveClanEvent;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.model.ClanRole;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanRepository;
import id.ac.ui.cs.advprog.yomu.social.validation.ClanValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClanMembershipServiceImpl implements ClanMembershipService {

    private final ClanRepository clanRepository;
    private final ClanMemberRepository memberRepository;
    private final ClanValidator clanValidator;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void joinClan(final String clanId, final String username, final String roleStr) {
        clanValidator.requireClanId(clanId);
        clanValidator.requireUsername(username);

        final String validClanId = Objects.requireNonNull(clanId);
        final Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(
                        SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        clanValidator.requireNotAlreadyMember(
                memberRepository.findByClanIdAndUsername(validClanId, username).isPresent());
        clanValidator.requireNotMemberOfOtherClan(
                memberRepository.findByUsername(username).isPresent());
        clanValidator.requireClanNotFull(memberRepository.countByClanId(validClanId));

        final ClanMember member = new ClanMember();
        member.setUsername(username);
        member.setClanId(validClanId);
        member.setRole(ClanRole.valueOf(roleStr));
        memberRepository.save(member);

        eventPublisher.publishEvent(new UserJoinClanEvent(this, username, validClanId, clan.getName(),
                clan.getTier() != null ? clan.getTier().name() : "BRONZE"));
    }

    @Override
    @Transactional
    public void leaveClan(final String clanId, final String username) {
        clanValidator.requireClanId(clanId);
        clanValidator.requireUsername(username);

        final String validClanId = Objects.requireNonNull(clanId);
        final Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(
                        SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        ClanMember member = memberRepository.findByClanIdAndUsername(validClanId, username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Member tidak ditemukan."));

        if (member.getRole() == ClanRole.LEADER) {
            handleLeaderLeave(clan, username);
        } else {
            memberRepository.deleteByClanIdAndUsername(validClanId, username);
            eventPublisher.publishEvent(new UserLeaveClanEvent(this, username, validClanId));
        }
    }

    private void handleLeaderLeave(final Clan clan, final String leaderUsername) {
        final List<ClanMember> allMembers = memberRepository.findByClanId(clan.getId());
        int minClanSize = SocialConstants.MIN_CLAN_SIZE;

        if (allMembers.size() <= minClanSize) {
            memberRepository.deleteByClanIdAndUsername(clan.getId(), leaderUsername);
            eventPublisher.publishEvent(new ClanShouldBeDeletedEvent(this, clan.getId(), leaderUsername));
        } else {
            final String newLeaderUsername = clanValidator.resolveReplacementLeader(allMembers,
                    leaderUsername);

            clan.setLeaderUsername(newLeaderUsername);
            clanRepository.save(clan);
            memberRepository.deleteByClanIdAndUsername(clan.getId(), leaderUsername);
        }

        eventPublisher.publishEvent(new UserLeaveClanEvent(this, leaderUsername, clan.getId()));
    }

    @Override
    @Transactional
    public void kickMember(String clanId, String leaderUsername, String memberUsername) {
        clanValidator.requireClanId(clanId);
        clanValidator.requireUsername(leaderUsername);
        clanValidator.requireUsername(memberUsername);

        final String validClanId = Objects.requireNonNull(clanId);

        Clan clan = clanRepository.findById(validClanId)
                .orElseThrow(() -> new IllegalArgumentException(
                        SocialConstants.CLAN_NOT_FOUND_MESSAGE));

        clanValidator.requireLeaderPrivilege(clan, leaderUsername,
                "Hanya Leader yang bisa mengeluarkan anggota");

        if (leaderUsername.equals(memberUsername)) {
            throw new IllegalArgumentException("Leader tidak bisa mengeluarkan diri sendiri");
        }

        memberRepository.deleteByClanIdAndUsername(clanId, memberUsername);
        eventPublisher.publishEvent(new UserLeaveClanEvent(this, memberUsername, clanId));
    }

    @Override
    public List<ClanMember> getMembersByClanId(final String clanId) {
        clanValidator.requireClanId(clanId);

        return memberRepository.getClanMembersByClanId(clanId).stream().toList();
    }

    @Override
    @Transactional
    public void deleteAllMembers(final String clanId) {
        clanValidator.requireClanId(clanId);
        memberRepository.deleteByClanId(clanId);
    }
}
