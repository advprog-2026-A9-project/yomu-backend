package id.ac.ui.cs.advprog.yomu.social.listener;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.social.event.ClanShouldBeDeletedEvent;
import id.ac.ui.cs.advprog.yomu.social.service.clan.lifecycle.ClanLifecycleService;

@ExtendWith(MockitoExtension.class)
class ClanLifecycleEventListenerTest {

    private static final String CLAN_ID = "clan-123";

    @Mock
    private ClanLifecycleService clanLifecycleService;

    @InjectMocks
    private ClanLifecycleEventListener listener;

    @Test
    void onClanShouldBeDeleted_ShouldDelegateToService() {
        ClanShouldBeDeletedEvent event = new ClanShouldBeDeletedEvent(this, CLAN_ID, "leader-123");

        listener.onClanShouldBeDeleted(event);

        verify(clanLifecycleService).deleteClan(CLAN_ID, "leader-123");
    }
}
