package id.ac.ui.cs.advprog.yomu.social.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.reading.event.QuizCompletedEvent;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.service.ClanModifierService;
import id.ac.ui.cs.advprog.yomu.social.service.ClanQuizStatsService;
import id.ac.ui.cs.advprog.yomu.social.service.ClanService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QuizCompletedEventListener {

    private final ClanMemberRepository memberRepository;
    private final ClanQuizStatsService statsService;
    private final ClanModifierService modifierService;
    private final ClanService clanService;

    @EventListener
    @Transactional
    public void onQuizCompleted(QuizCompletedEvent event) {
        memberRepository.findByUserId(event.userId())
                .map(member -> member.getClanId())
                .filter(clanId -> clanId != null && !clanId.isBlank())
                .ifPresent(clanId -> {
                    var stats = statsService.recordQuizResult(
                            clanId,
                            event.correctAnswers(),
                            event.totalQuestions(),
                            event.score());
                    modifierService.evaluateModifiers(clanId, stats);
                    clanService.updateClanScore(clanId, event.score());
                });
    }
}
