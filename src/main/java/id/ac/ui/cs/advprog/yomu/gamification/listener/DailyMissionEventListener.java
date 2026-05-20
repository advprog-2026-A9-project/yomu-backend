package id.ac.ui.cs.advprog.yomu.gamification.listener;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.gamification.event.DailyMissionCompletedEvent;
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserDailyMissionProgress;
import id.ac.ui.cs.advprog.yomu.gamification.service.mission.DailyMissionProgressService;
import id.ac.ui.cs.advprog.yomu.gamification.service.mission.DailyMissionRotationService;
import id.ac.ui.cs.advprog.yomu.reading.event.QuizCompletedEvent;
import id.ac.ui.cs.advprog.yomu.reading.event.ReadingCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyMissionEventListener {

    private final DailyMissionProgressService dailyMissionProgressService;
    private final DailyMissionRotationService dailyMissionRotationService;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Transactional
    public void onQuizCompleted(QuizCompletedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("DailyMissionEventListener received QuizCompletedEvent for userId={} score={}", event.userId(), event.score());
        }
        processMissionsForEvent(event.userId(), DailyMission.EventType.QUIZ_COMPLETED, event.score());
    }

    @EventListener
    @Transactional
    public void onReadingCompleted(ReadingCompletedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("DailyMissionEventListener received ReadingCompletedEvent for username={}", event.getUsername());
        }
        processMissionsForEvent(event.getUsername(), DailyMission.EventType.READING_COMPLETED, 0);
    }

    private void processMissionsForEvent(String username, DailyMission.EventType eventType, int score) {
        LocalDate today = LocalDate.now();
        for (DailyMission mission : dailyMissionRotationService.getActiveDailyMissions(today)) {
            if (!mission.isEligibleForEvent(eventType)) {
                continue;
            }
            if (!mission.isEligibleForUpdate(score)) {
                continue;
            }

            UserDailyMissionProgress progress = dailyMissionProgressService
                    .getOrCreateMissionProgress(username, mission, today);

            if (!progress.isCompleted()) {
                progress.setProgressValue(mission.calculateNewProgressValue(progress.getProgressValue()));
                int targetCountVal = mission.getTargetValue();

                if (progress.getProgressValue() >= targetCountVal) {
                    progress.setCompleted(true);
                    progress.setCompletedAt(LocalDateTime.now());
                    eventPublisher.publishEvent(
                            new DailyMissionCompletedEvent(username,
                                    mission.getRewardScore()));
                }
                dailyMissionProgressService.saveProgress(progress);
            }
        }
    }
}
