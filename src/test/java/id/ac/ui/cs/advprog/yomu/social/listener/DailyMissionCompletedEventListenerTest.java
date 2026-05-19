package id.ac.ui.cs.advprog.yomu.social.listener;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.gamification.event.AllDailyMissionsCompletedEvent;
import id.ac.ui.cs.advprog.yomu.gamification.event.DailyMissionCompletedEvent;
import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.service.modifier.buff.BuffApplicationService;
import id.ac.ui.cs.advprog.yomu.social.service.score.ClanScoreService;

@ExtendWith(MockitoExtension.class)
class DailyMissionCompletedEventListenerTest {

    private static final String CLAN_ID = "clan-1";
    private static final String USER_ID = "user-1";

    @Mock
    private ClanMemberRepository memberRepository;

    @Mock
    private ClanScoreService scoreService;

    @Mock
    private BuffApplicationService buffApplicationService;

    @InjectMocks
    private DailyMissionCompletedEventListener listener;

    @Test
    void onDailyMissionCompleted_WhenUserInClan_ShouldUpdateClanScore() {
        ClanMember member = new ClanMember();
        member.setUsername(USER_ID);
        member.setClanId(CLAN_ID);

        DailyMissionCompletedEvent event = new DailyMissionCompletedEvent(USER_ID, 150);

        when(memberRepository.findByUsername(USER_ID)).thenReturn(Optional.of(member));

        listener.onDailyMissionCompleted(event);

        verify(scoreService).updateClanScore(CLAN_ID, 150);
    }

    @Test
    void onDailyMissionCompleted_WhenNoClanMembership_ShouldDoNothing() {
        DailyMissionCompletedEvent event = new DailyMissionCompletedEvent("user-2", 200);

        when(memberRepository.findByUsername("user-2")).thenReturn(Optional.empty());

        listener.onDailyMissionCompleted(event);

        verifyNoInteractions(scoreService);
    }

    @Test
    void onDailyMissionCompleted_WhenClanIdBlank_ShouldDoNothing() {
        ClanMember member = new ClanMember();
        member.setUsername("user-3");
        member.setClanId(" ");

        DailyMissionCompletedEvent event = new DailyMissionCompletedEvent("user-3", 100);

        when(memberRepository.findByUsername("user-3")).thenReturn(Optional.of(member));

        listener.onDailyMissionCompleted(event);

        verifyNoInteractions(scoreService);
    }

    @Test
    void onAllDailyMissionsCompleted_WhenUserInClan_ShouldDelegateToBuffApplicationService() {
        ClanMember member = new ClanMember();
        member.setUsername(USER_ID);
        member.setClanId(CLAN_ID);

        AllDailyMissionsCompletedEvent event = new AllDailyMissionsCompletedEvent(USER_ID, LocalDate.now());

        when(memberRepository.findByUsername(USER_ID)).thenReturn(Optional.of(member));

        listener.onAllDailyMissionsCompleted(event);

        verify(buffApplicationService).applyBuff(CLAN_ID, SocialConstants.DAILY_MISSION_BUFF_KEY);
    }

    @Test
    void onAllDailyMissionsCompleted_WhenNoClanMembership_ShouldDoNothing() {
        AllDailyMissionsCompletedEvent event = new AllDailyMissionsCompletedEvent("user-2", LocalDate.now());

        when(memberRepository.findByUsername("user-2")).thenReturn(Optional.empty());

        listener.onAllDailyMissionsCompleted(event);

        verifyNoInteractions(buffApplicationService);
    }
}
