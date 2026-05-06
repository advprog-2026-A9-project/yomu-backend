package id.ac.ui.cs.advprog.yomu.social.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.auth.event.UserDeletedEvent;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.service.ClanService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserDeletedEventListener {

    private final ClanMemberRepository memberRepository;
    private final ClanService clanService;

    @EventListener
    @Transactional
    public void onUserDeleted(UserDeletedEvent event) {
        memberRepository.findByUserId(event.getUserId())
                .map(member -> member.getClanId())
                .filter(clanId -> clanId != null && !clanId.isBlank())
            .ifPresent(clanId -> clanService.leaveClan(clanId, event.getUserId()));
    }
}
