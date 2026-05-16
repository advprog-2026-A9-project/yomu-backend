package id.ac.ui.cs.advprog.yomu.gamification.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.gamification.service.ProgressTrackingService;
import id.ac.ui.cs.advprog.yomu.reading.event.QuizCompletedEvent;
import lombok.RequiredArgsConstructor;

/**
 * Listener for QuizCompletedEvent to update gamification progress
 * Decoupled from the reading module via Spring Events
 */
@Component
@RequiredArgsConstructor
public class GamificationQuizListener {

    private final ProgressTrackingService progressTrackingService;

    @EventListener
    @Transactional
    public void onQuizCompleted(QuizCompletedEvent event) {
        progressTrackingService.handleQuizCompletion(event.userId(), event.score());
    }
}
