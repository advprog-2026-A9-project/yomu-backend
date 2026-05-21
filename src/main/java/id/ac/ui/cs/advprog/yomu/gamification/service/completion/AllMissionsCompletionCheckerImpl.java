package id.ac.ui.cs.advprog.yomu.gamification.service.completion;

import java.time.LocalDate;
import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.gamification.event.AllDailyMissionsCompletedEventPublisher;
import id.ac.ui.cs.advprog.yomu.gamification.event.DailyMissionCompletedEvent;
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserDailyMissionProgress;
import id.ac.ui.cs.advprog.yomu.gamification.repository.DailyMissionRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserDailyMissionProgressRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class AllMissionsCompletionCheckerImpl implements AllMissionsCompletionChecker {

    private final DailyMissionRepository dailyMissionRepository;
    private final UserDailyMissionProgressRepository userDailyMissionProgressRepository;
    private final AllDailyMissionsCompletedEventPublisher allDailyMissionsCompletedEventPublisher;

    @Override
    @Transactional(readOnly = true)
    public boolean areAllActiveDailyMissionsCompleted(String username, LocalDate progressDate) {
        List<DailyMission> activeMissions = dailyMissionRepository
                .findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(progressDate, progressDate);

        if (activeMissions.isEmpty()) {
            return false;
        }

        return activeMissions.stream().allMatch(mission -> userDailyMissionProgressRepository
                .findByUsernameAndDailyMissionAndProgressDate(username, mission, progressDate)
                .map(UserDailyMissionProgress::isCompleted)
                .orElse(false));
    }

    @EventListener
    @Transactional
    public void onDailyMissionCompleted(DailyMissionCompletedEvent event) {
        String username = event.username();
        LocalDate today = LocalDate.now();
        if (areAllActiveDailyMissionsCompleted(username, today)) {
            allDailyMissionsCompletedEventPublisher.publish(username, today);
        }
    }
}
