package id.ac.ui.cs.advprog.yomu.gamification.listener;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import id.ac.ui.cs.advprog.yomu.gamification.event.DailyMissionCompletedEvent;
import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedDailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserDailyMissionProgress;
import id.ac.ui.cs.advprog.yomu.gamification.service.mission.DailyMissionProgressService;
import id.ac.ui.cs.advprog.yomu.gamification.service.mission.DailyMissionRotationService;
import id.ac.ui.cs.advprog.yomu.reading.event.QuizCompletedEvent;

@ExtendWith(MockitoExtension.class)
class DailyMissionEventListenerTest {

    private static final String USER_ID = "user-1";
    private static final LocalDate TODAY = LocalDate.now();

    @Mock
    private DailyMissionProgressService dailyMissionProgressService;

    @Mock
    private DailyMissionRotationService dailyMissionRotationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private DailyMissionEventListener listener;
    private CountBasedDailyMission quizMission;

    @BeforeEach
    void setUp() {
        listener = new DailyMissionEventListener(
            dailyMissionProgressService,
            dailyMissionRotationService,
            eventPublisher
        );

        quizMission = new CountBasedDailyMission();
        quizMission.setId("mission-1");
        quizMission.setName("Quiz Starter");
        quizMission.setMilestone("Complete one quiz today");
        quizMission.setMissionType("complete_n_quizzes");
        quizMission.setTargetCount(1);
        quizMission.setRewardScore(10);
        quizMission.setActiveFrom(TODAY);
        quizMission.setActiveUntil(TODAY);
        quizMission.setActive(true);
    }

    @Test
    void onQuizCompleted_ShouldUpdateQuizMissionAndPublishEvent() {
        UserDailyMissionProgress progress = new UserDailyMissionProgress();
        progress.setUsername(USER_ID);
        progress.setDailyMission(quizMission);
        progress.setProgressDate(TODAY);
        progress.setProgressValue(0);
        progress.setCompleted(false);

        when(dailyMissionRotationService.getActiveDailyMissions(TODAY)).thenReturn(List.of(quizMission));
        when(dailyMissionProgressService.getOrCreateMissionProgress(USER_ID, quizMission, TODAY)).thenReturn(progress);

        listener.onQuizCompleted(new QuizCompletedEvent(USER_ID, 101L, 100, 1, 1));

        assertTrue(progress.isCompleted());
        assertEquals(1, progress.getProgressValue());
        verify(dailyMissionProgressService).saveProgress(progress);
        verify(eventPublisher).publishEvent(any(DailyMissionCompletedEvent.class));
    }
}
