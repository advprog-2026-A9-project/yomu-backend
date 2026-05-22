package id.ac.ui.cs.advprog.yomu.social.listener;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.auth.event.UserDeletedEvent;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.service.clan.membership.ClanMembershipService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class UserDeletedEventListenerTest {

    private static final String USER_4 = "user-4";

    @Mock
    private ClanMemberRepository memberRepository;

    @Mock
    private ClanMembershipService membershipService;

    private UserDeletedEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new UserDeletedEventListener(memberRepository, membershipService);
    }

    @Test
    void onUserDeleted_WhenMemberNotFound_ShouldDoNothing() {
        when(memberRepository.findByUsername("user-1")).thenReturn(Optional.empty());

        listener.onUserDeleted(new UserDeletedEvent(this, "id-1", "user-1"));

        verify(membershipService, never()).leaveClan(anyString(), anyString());
    }

    @Test
    void onUserDeleted_WhenClanIdIsNull_ShouldDoNothing() {
        ClanMember member = new ClanMember();
        member.setUsername("user-2");
        member.setClanId(null);

        when(memberRepository.findByUsername("user-2")).thenReturn(Optional.of(member));

        listener.onUserDeleted(new UserDeletedEvent(this, "id-2", "user-2"));

        verify(membershipService, never()).leaveClan(anyString(), anyString());
    }

    @Test
    void onUserDeleted_WhenClanIdIsBlank_ShouldDoNothing() {
        ClanMember member = new ClanMember();
        member.setUsername("user-3");
        member.setClanId("   ");

        when(memberRepository.findByUsername("user-3")).thenReturn(Optional.of(member));

        listener.onUserDeleted(new UserDeletedEvent(this, "id-3", "user-3"));

        verify(membershipService, never()).leaveClan(anyString(), anyString());
    }

    @Test
    void onUserDeleted_WhenMemberHasClanId_ShouldLeaveClan() {
        ClanMember member = new ClanMember();
        member.setUsername(USER_4);
        member.setClanId("clan-abc");

        when(memberRepository.findByUsername(USER_4)).thenReturn(Optional.of(member));

        listener.onUserDeleted(new UserDeletedEvent(this, "id-4", USER_4));

        verify(membershipService).leaveClan("clan-abc", USER_4);
    }
}
