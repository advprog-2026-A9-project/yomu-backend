package id.ac.ui.cs.advprog.yomu.gamification.listener;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;
import id.ac.ui.cs.advprog.yomu.gamification.service.achievement.AchievementProgressService;
import id.ac.ui.cs.advprog.yomu.gamification.service.achievement.AchievementService;
import id.ac.ui.cs.advprog.yomu.gamification.strategy.AchievementProgressEvaluator;
import id.ac.ui.cs.advprog.yomu.gamification.strategy.CountBasedAchievementEvaluator;
import id.ac.ui.cs.advprog.yomu.reading.event.QuizCompletedEvent;

@ExtendWith(MockitoExtension.class)
class AchievementEventListenerTest {

    private static final String USER_ID = "user-1";

    @Mock
    private AchievementService achievementService;

    @Mock
    private AchievementProgressService achievementProgressService;

    private AchievementEventListener listener;
    private CountBasedAchievement quizAch;

    @BeforeEach
    void setUp() {
        List<AchievementProgressEvaluator> evaluators = List.of(
            new CountBasedAchievementEvaluator()
        );

        listener = new AchievementEventListener(
            achievementService,
            achievementProgressService,
            evaluators
        );

        quizAch = new CountBasedAchievement();
        quizAch.setId("achievement-quiz-1");
        quizAch.setName("Quiz Starter");
        quizAch.setMilestone("Pass one quiz");
        quizAch.setMilestoneType("quizzes_passed");
        quizAch.setMilestoneThreshold(1);
        quizAch.setActive(true);
    }

    @Test
    void onQuizCompleted_ShouldIncrementQuizAchievementProgress() {
        UserAchievementProgress progress = new UserAchievementProgress();
        progress.setUsername(USER_ID);
        progress.setAchievement(quizAch);
        progress.setProgressValue(0);
        progress.setUnlocked(false);

        when(achievementService.getAllAchievements()).thenReturn(List.of(quizAch));
        when(achievementProgressService.getOrCreateAchievementProgress(USER_ID, quizAch)).thenReturn(progress);

        listener.onQuizCompleted(new QuizCompletedEvent(USER_ID, 101L, 90, 1, 1));

        assertTrue(progress.isUnlocked());
        assertEquals(1, progress.getProgressValue());
        verify(achievementProgressService).saveProgress(progress);
    }
}
