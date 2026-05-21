package id.ac.ui.cs.advprog.yomu.social.listener;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.event.ClanCreatedEvent;
import id.ac.ui.cs.advprog.yomu.social.event.JoinRequestAcceptedEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserDeleteClanEvent;
import id.ac.ui.cs.advprog.yomu.social.service.clan.membership.ClanMembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClanMembershipEventListener {

    private final ClanMembershipService clanMembershipService;

    @EventListener
    @Transactional
    public void onJoinRequestAccepted(JoinRequestAcceptedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Handling JoinRequestAcceptedEvent for user: {} and clan: {}", event.getUsername(), event.getClanId());
        }
        clanMembershipService.joinClan(event.getClanId(), event.getUsername(), SocialConstants.ROLE_MEMBER);
    }

    @EventListener
    @Transactional
    public void onClanCreated(ClanCreatedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Handling ClanCreatedEvent for leader: {} and clan: {}", event.getUsername(), event.getClanId());
        }
        clanMembershipService.joinClan(event.getClanId(), event.getUsername(), SocialConstants.ROLE_LEADER);
    }

    @EventListener
    @Transactional
    public void onClanDeleted(UserDeleteClanEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Handling UserDeleteClanEvent to delete all members of clan: {}", event.getClanId());
        }
        clanMembershipService.deleteAllMembers(event.getClanId());
    }
}
