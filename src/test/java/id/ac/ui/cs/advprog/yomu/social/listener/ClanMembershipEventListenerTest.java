package id.ac.ui.cs.advprog.yomu.social.listener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.event.ClanCreatedEvent;
import id.ac.ui.cs.advprog.yomu.social.event.JoinRequestAcceptedEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserDeleteClanEvent;
import id.ac.ui.cs.advprog.yomu.social.service.clan.membership.ClanMembershipService;

@ExtendWith(MockitoExtension.class)
class ClanMembershipEventListenerTest {

    private static final String CLAN_ID = "clan-123";
    private static final String USERNAME = "user-123";
    private static final String CLAN_NAME = "My Clan";

    @Mock
    private ClanMembershipService clanMembershipService;

    @InjectMocks
    private ClanMembershipEventListener listener;

    @Test
    void onJoinRequestAccepted_ShouldDelegateToService() {
        JoinRequestAcceptedEvent event = new JoinRequestAcceptedEvent(this, USERNAME, CLAN_ID, CLAN_NAME);

        listener.onJoinRequestAccepted(event);

        verify(clanMembershipService).joinClan(CLAN_ID, USERNAME, id.ac.ui.cs.advprog.yomu.social.model.ClanRole.MEMBER.toString());
    }

    @Test
    void onClanCreated_ShouldDelegateToService() {
        ClanCreatedEvent event = new ClanCreatedEvent(this, USERNAME, CLAN_ID, CLAN_NAME, "BRONZE");

        listener.onClanCreated(event);

        verify(clanMembershipService).joinClan(CLAN_ID, USERNAME, id.ac.ui.cs.advprog.yomu.social.model.ClanRole.LEADER.toString());
    }

    @Test
    void onClanDeleted_ShouldDelegateToService() {
        UserDeleteClanEvent event = new UserDeleteClanEvent(this, CLAN_ID);

        listener.onClanDeleted(event);

        verify(clanMembershipService).deleteAllMembers(CLAN_ID);
    }
}
