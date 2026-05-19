package id.ac.ui.cs.advprog.yomu.social.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.reading.event.QuizCompletedEvent;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.service.modifier.ClanModifierService;
import id.ac.ui.cs.advprog.yomu.social.service.score.ClanQuizStatsService;
import id.ac.ui.cs.advprog.yomu.social.service.score.ClanScoreService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QuizCompletedEventListener {

    private final ClanMemberRepository memberRepository;
    private final ClanQuizStatsService statsService;
    private final ClanModifierService modifierService;
    private final ClanScoreService scoreService;

    @EventListener
    @Transactional
    public void onQuizCompleted(QuizCompletedEvent event) {
        memberRepository.findByUsername(event.userId())
                .map(member -> member.getClanId())
                .filter(clanId -> clanId != null && !clanId.isBlank())
                .ifPresent(clanId -> {
                    var stats = statsService.recordQuizResult(
                            clanId,
                            event.correctAnswers(),
                            event.totalQuestions(),
                            event.score());
                    modifierService.evaluateModifiers(clanId, stats);
                    scoreService.updateClanScore(clanId, event.score());
                });
    }
}
