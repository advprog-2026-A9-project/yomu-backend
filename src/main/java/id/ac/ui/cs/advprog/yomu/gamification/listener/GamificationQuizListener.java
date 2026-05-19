package id.ac.ui.cs.advprog.yomu.gamification.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.gamification.service.ProgressTrackingService;
import id.ac.ui.cs.advprog.yomu.reading.event.QuizCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Listener for QuizCompletedEvent to update gamification progress
 * Decoupled from the reading module via Spring Events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GamificationQuizListener {

    private final ProgressTrackingService progressTrackingService;

    @EventListener
    @Transactional
    public void onQuizCompleted(QuizCompletedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("GamificationQuizListener received QuizCompletedEvent for userId={} score={}", event.userId(), event.score());
        }
        progressTrackingService.handleQuizCompletion(event.userId(), event.score());
    }
}
