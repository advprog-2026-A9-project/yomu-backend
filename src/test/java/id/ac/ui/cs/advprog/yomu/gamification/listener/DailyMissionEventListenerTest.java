package id.ac.ui.cs.advprog.yomu.gamification.listener;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import id.ac.ui.cs.advprog.yomu.gamification.event.DailyMissionCompletedEvent;
import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedDailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserDailyMissionProgress;
import id.ac.ui.cs.advprog.yomu.gamification.service.mission.DailyMissionProgressService;
import id.ac.ui.cs.advprog.yomu.gamification.service.mission.DailyMissionRotationService;
import id.ac.ui.cs.advprog.yomu.reading.event.QuizCompletedEvent;
import id.ac.ui.cs.advprog.yomu.reading.event.ReadingCompletedEvent;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class DailyMissionEventListenerTest {

    private static final String USER_ID = "user-1";
    private static final LocalDate TODAY = LocalDate.now();

    @Mock
    private DailyMissionProgressService dailyMissionProgressService;

    @Mock
    private DailyMissionRotationService dailyMissionRotationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private DailyMission mockMission;

    private DailyMissionEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new DailyMissionEventListener(
                dailyMissionProgressService,
                dailyMissionRotationService,
                eventPublisher
        );
    }

    @Test
    void onQuizCompleted_WhenNotEligibleForEvent_ShouldSkip() {
        when(dailyMissionRotationService.getActiveDailyMissions(TODAY)).thenReturn(List.of(mockMission));
        when(mockMission.isEligibleForEvent(DailyMission.EventType.QUIZ_COMPLETED)).thenReturn(false);

        listener.onQuizCompleted(new QuizCompletedEvent(USER_ID, 101L, 90, 1, 1));

        verify(dailyMissionProgressService, never()).getOrCreateMissionProgress(any(), any(), any());
    }

    @Test
    void onQuizCompleted_WhenNotEligibleForUpdate_ShouldSkip() {
        when(dailyMissionRotationService.getActiveDailyMissions(TODAY)).thenReturn(List.of(mockMission));
        when(mockMission.isEligibleForEvent(DailyMission.EventType.QUIZ_COMPLETED)).thenReturn(true);
        when(mockMission.isEligibleForUpdate(90)).thenReturn(false);

        listener.onQuizCompleted(new QuizCompletedEvent(USER_ID, 101L, 90, 1, 1));

        verify(dailyMissionProgressService, never()).getOrCreateMissionProgress(any(), any(), any());
    }

    @Test
    void onQuizCompleted_WhenAlreadyCompleted_ShouldNotIncrementOrSave() {
        UserDailyMissionProgress progress = new UserDailyMissionProgress();
        progress.setCompleted(true);

        when(dailyMissionRotationService.getActiveDailyMissions(TODAY)).thenReturn(List.of(mockMission));
        when(mockMission.isEligibleForEvent(DailyMission.EventType.QUIZ_COMPLETED)).thenReturn(true);
        when(mockMission.isEligibleForUpdate(90)).thenReturn(true);
        when(dailyMissionProgressService.getOrCreateMissionProgress(USER_ID, mockMission, TODAY)).thenReturn(progress);

        listener.onQuizCompleted(new QuizCompletedEvent(USER_ID, 101L, 90, 1, 1));

        assertAll("Verify no progress increment or saving is made",
                () -> verify(mockMission, never()).calculateNewProgressValue(any(Integer.class)),
                () -> verify(dailyMissionProgressService, never()).saveProgress(any()));
    }

    @Test
    void onReadingCompleted_WhenEligible_ShouldIncrementAndPublishEventOnCompletion() {
        UserDailyMissionProgress progress = new UserDailyMissionProgress();
        progress.setCompleted(false);
        progress.setProgressValue(0);

        when(dailyMissionRotationService.getActiveDailyMissions(TODAY)).thenReturn(List.of(mockMission));
        when(mockMission.isEligibleForEvent(DailyMission.EventType.READING_COMPLETED)).thenReturn(true);
        when(mockMission.isEligibleForUpdate(0)).thenReturn(true);
        when(dailyMissionProgressService.getOrCreateMissionProgress(USER_ID, mockMission, TODAY)).thenReturn(progress);
        
        when(mockMission.calculateNewProgressValue(0)).thenReturn(2);
        when(mockMission.getTargetValue()).thenReturn(2);
        when(mockMission.getRewardScore()).thenReturn(50);

        listener.onReadingCompleted(new ReadingCompletedEvent(this, 101L, USER_ID));

        assertAll("Verify progress updates and mock interactions",
                () -> assertTrue(progress.isCompleted(), "Mission should be completed"),
                () -> assertEquals(2, progress.getProgressValue(), "Progress value should be updated to 2"),
                () -> verify(eventPublisher).publishEvent(any(DailyMissionCompletedEvent.class)),
                () -> verify(dailyMissionProgressService).saveProgress(progress));
    }
}
