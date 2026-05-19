package id.ac.ui.cs.advprog.yomu.gamification.event;

import java.time.LocalDate;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AllDailyMissionsCompletedEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publish(String username, LocalDate progressDate) {
        eventPublisher.publishEvent(new AllDailyMissionsCompletedEvent(username, progressDate));
    }
}