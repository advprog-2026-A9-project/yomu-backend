package id.ac.ui.cs.advprog.yomu.gamification.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.gamification.event.AllDailyMissionsCompletedEventPublisher;
import id.ac.ui.cs.advprog.yomu.gamification.event.DailyMissionCompletedEvent;
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedDailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserDailyMissionProgress;
import id.ac.ui.cs.advprog.yomu.gamification.repository.DailyMissionRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserDailyMissionProgressRepository;
import id.ac.ui.cs.advprog.yomu.gamification.service.completion.AllMissionsCompletionCheckerImpl;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AllMissionsCompletionCheckerImplTest {

    private static final String USERNAME = "test-user";
    private static final LocalDate TODAY = LocalDate.now();

    @Mock
    private DailyMissionRepository dailyMissionRepository;

    @Mock
    private UserDailyMissionProgressRepository userDailyMissionProgressRepository;

    @Mock
    private AllDailyMissionsCompletedEventPublisher allDailyMissionsCompletedEventPublisher;

    @InjectMocks
    private AllMissionsCompletionCheckerImpl checker;

    private DailyMission mission1;
    private DailyMission mission2;

    @BeforeEach
    void setUp() {
        CountBasedDailyMission m1 = new CountBasedDailyMission();
        m1.setId("m-1");
        m1.setActive(true);
        mission1 = m1;

        CountBasedDailyMission m2 = new CountBasedDailyMission();
        m2.setId("m-2");
        m2.setActive(true);
        mission2 = m2;
    }

    @Test
    void onDailyMissionCompleted_WhenAllMissionsCompleted_ShouldPublishEvent() {
        DailyMissionCompletedEvent event = new DailyMissionCompletedEvent(USERNAME, 10);

        when(dailyMissionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(TODAY, TODAY))
                .thenReturn(List.of(mission1, mission2));

        UserDailyMissionProgress progress1 = new UserDailyMissionProgress();
        progress1.setCompleted(true);

        UserDailyMissionProgress progress2 = new UserDailyMissionProgress();
        progress2.setCompleted(true);

        when(userDailyMissionProgressRepository.findByUsernameAndDailyMissionAndProgressDate(USERNAME, mission1, TODAY))
                .thenReturn(Optional.of(progress1));
        when(userDailyMissionProgressRepository.findByUsernameAndDailyMissionAndProgressDate(USERNAME, mission2, TODAY))
                .thenReturn(Optional.of(progress2));

        checker.onDailyMissionCompleted(event);

        verify(allDailyMissionsCompletedEventPublisher).publish(USERNAME, TODAY);
    }

    @Test
    void onDailyMissionCompleted_WhenNotAllMissionsCompleted_ShouldNotPublishEvent() {
        DailyMissionCompletedEvent event = new DailyMissionCompletedEvent(USERNAME, 10);

        when(dailyMissionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(TODAY, TODAY))
                .thenReturn(List.of(mission1, mission2));

        UserDailyMissionProgress progress1 = new UserDailyMissionProgress();
        progress1.setCompleted(true);

        UserDailyMissionProgress progress2 = new UserDailyMissionProgress();
        progress2.setCompleted(false);

        when(userDailyMissionProgressRepository.findByUsernameAndDailyMissionAndProgressDate(USERNAME, mission1, TODAY))
                .thenReturn(Optional.of(progress1));
        when(userDailyMissionProgressRepository.findByUsernameAndDailyMissionAndProgressDate(USERNAME, mission2, TODAY))
                .thenReturn(Optional.of(progress2));

        checker.onDailyMissionCompleted(event);

        verify(allDailyMissionsCompletedEventPublisher, never()).publish(USERNAME, TODAY);
    }

    @Test
    void onDailyMissionCompleted_WhenNoActiveMissions_ShouldNotPublishEvent() {
        DailyMissionCompletedEvent event = new DailyMissionCompletedEvent(USERNAME, 10);

        when(dailyMissionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(TODAY, TODAY))
                .thenReturn(List.of());

        checker.onDailyMissionCompleted(event);

        verify(allDailyMissionsCompletedEventPublisher, never()).publish(USERNAME, TODAY);
    }
}
