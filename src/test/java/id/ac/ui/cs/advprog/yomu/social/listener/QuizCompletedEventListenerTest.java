package id.ac.ui.cs.advprog.yomu.social.listener;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.reading.event.QuizCompletedEvent;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.service.ClanModifierService;
import id.ac.ui.cs.advprog.yomu.social.service.ClanQuizStatsService;
import id.ac.ui.cs.advprog.yomu.social.service.ClanService;

@ExtendWith(MockitoExtension.class)
class QuizCompletedEventListenerTest {

    private static final String CLAN_ID = "clan-1";
    private static final String USER_ID = "user-1";

    @Mock
    private ClanMemberRepository memberRepository;

    @Mock
    private ClanQuizStatsService statsService;

    @Mock
    private ClanModifierService modifierService;

    @Mock
    private ClanService clanService;

    @InjectMocks
    private QuizCompletedEventListener listener;

    @Test
    void onQuizCompleted_WhenUserInClan_ShouldUpdateStatsModifierAndScore() {
        ClanMember member = new ClanMember();
        member.setUserId(USER_ID);
        member.setClanId(CLAN_ID);

        ClanQuizStats stats = new ClanQuizStats();
        stats.setClanId(CLAN_ID);

        QuizCompletedEvent event = new QuizCompletedEvent(USER_ID, 1L, 80, 8, 10);

        when(memberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(member));
        when(statsService.recordQuizResult(CLAN_ID, 8, 10, 80)).thenReturn(stats);

        listener.onQuizCompleted(event);

        assertAll("Verify interactions with services",
                () -> verify(statsService).recordQuizResult(CLAN_ID, 8, 10, 80),
                () -> verify(modifierService).evaluateModifiers(CLAN_ID, stats),
                () -> verify(clanService).updateClanScore(CLAN_ID, 80));
    }

    @Test
    void onQuizCompleted_WhenNoClanMembership_ShouldDoNothing() {
        QuizCompletedEvent event = new QuizCompletedEvent("user-2", 2L, 60, 6, 10);

        when(memberRepository.findByUserId("user-2")).thenReturn(Optional.empty());

        listener.onQuizCompleted(event);

        verifyNoInteractions(statsService, modifierService, clanService);
    }

    @Test
    void onQuizCompleted_WhenClanIdBlank_ShouldDoNothing() {
        ClanMember member = new ClanMember();
        member.setUserId("user-3");
        member.setClanId(" ");

        QuizCompletedEvent event = new QuizCompletedEvent("user-3", 3L, 70, 7, 10);

        when(memberRepository.findByUserId("user-3")).thenReturn(Optional.of(member));

        listener.onQuizCompleted(event);

        verifyNoInteractions(statsService, modifierService, clanService);
    }
}
