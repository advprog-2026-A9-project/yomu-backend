package id.ac.ui.cs.advprog.yomu.gamification.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.gamification.service.ProgressTrackingService;
import id.ac.ui.cs.advprog.yomu.reading.event.ReadingCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Listener for ReadingCompletedEvent to update gamification progress
 * Decoupled from the reading module via Spring Events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GamificationReadingListener {

    private final ProgressTrackingService progressTrackingService;

    @EventListener
    @Transactional
    public void onReadingCompleted(ReadingCompletedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("GamificationReadingListener received ReadingCompletedEvent for username={}", event.getUsername());
        }
        progressTrackingService.handleReadingCompletion(event.getUsername());
    }
}
