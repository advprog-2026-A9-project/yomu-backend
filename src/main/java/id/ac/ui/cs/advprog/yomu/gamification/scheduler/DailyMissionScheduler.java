package id.ac.ui.cs.advprog.yomu.gamification.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.gamification.service.DailyMissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduler to handle daily mission rotation at midnight
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DailyMissionScheduler {

    private final DailyMissionService dailyMissionService;

    /**
     * Rotates daily missions at midnight every day
     * Cron expression: second, minute, hour, day of month, month, day of week
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void rotateMissions() {
        log.info("Starting daily mission rotation...");
        dailyMissionService.rotateMissions();
        log.info("Daily mission rotation completed.");
    }
}
