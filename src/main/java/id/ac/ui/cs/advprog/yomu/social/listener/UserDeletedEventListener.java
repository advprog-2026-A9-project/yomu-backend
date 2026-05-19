package id.ac.ui.cs.advprog.yomu.social.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.auth.event.UserDeletedEvent;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.service.clan.membership.ClanMembershipService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserDeletedEventListener {

    private final ClanMemberRepository memberRepository;
    private final ClanMembershipService membershipService;

    @EventListener
    @Transactional
    public void onUserDeleted(UserDeletedEvent event) {
        memberRepository.findByUsername(event.getUsername())
                .map(member -> member.getClanId())
                .filter(clanId -> clanId != null && !clanId.isBlank())
                .ifPresent(clanId -> membershipService.leaveClan(clanId, event.getUsername()));
    }
}
