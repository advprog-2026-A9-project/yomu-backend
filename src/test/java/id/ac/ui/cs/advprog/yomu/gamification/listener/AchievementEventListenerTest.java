package id.ac.ui.cs.advprog.yomu.gamification.listener;

import java.util.List;

<<<<<<< HEAD
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
=======
>>>>>>> origin/staging
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
<<<<<<< HEAD
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
=======
import org.mockito.Mock;
>>>>>>> origin/staging
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;
import id.ac.ui.cs.advprog.yomu.gamification.service.achievement.AchievementProgressService;
import id.ac.ui.cs.advprog.yomu.gamification.service.achievement.AchievementService;
import id.ac.ui.cs.advprog.yomu.gamification.strategy.AchievementProgressEvaluator;
<<<<<<< HEAD
import id.ac.ui.cs.advprog.yomu.reading.event.QuizCompletedEvent;
import id.ac.ui.cs.advprog.yomu.reading.event.ReadingCompletedEvent;
import id.ac.ui.cs.advprog.yomu.social.event.SeasonRankingEvent;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AchievementEventListenerTest {

    private static final String USER_ID = "user-1";
    private static final String QUIZZES_PASSED = "quizzes_passed";
=======
import id.ac.ui.cs.advprog.yomu.gamification.strategy.CountBasedAchievementEvaluator;
import id.ac.ui.cs.advprog.yomu.reading.event.QuizCompletedEvent;

@ExtendWith(MockitoExtension.class)
class AchievementEventListenerTest {

    private static final String USER_ID = "user-1";
    private static final String ASSERTION_MESSAGE = "quiz achievement should increment and unlock";
>>>>>>> origin/staging

    @Mock
    private AchievementService achievementService;

    @Mock
    private AchievementProgressService achievementProgressService;

<<<<<<< HEAD
    @Mock
    private AchievementProgressEvaluator mockEvaluator;

    private AchievementEventListener listener;
    private CountBasedAchievement activeAch;
    private CountBasedAchievement inactiveAch;

    @BeforeEach
    void setUp() {
        listener = new AchievementEventListener(
                achievementService,
                achievementProgressService,
                List.of(mockEvaluator)
        );

        activeAch = new CountBasedAchievement();
        activeAch.setId("active-1");
        activeAch.setMilestoneType(QUIZZES_PASSED);
        activeAch.setActive(true);

        inactiveAch = new CountBasedAchievement();
        inactiveAch.setId("inactive-1");
        inactiveAch.setMilestoneType(QUIZZES_PASSED);
        inactiveAch.setActive(false);
    }

    @Test
    void onQuizCompleted_WhenEvaluatorMatchesAndSaves() {
        UserAchievementProgress progress = new UserAchievementProgress();
        progress.setAchievement(activeAch);

        when(achievementService.getAllAchievements()).thenReturn(List.of(activeAch, inactiveAch));
        when(achievementProgressService.getOrCreateAchievementProgress(USER_ID, activeAch)).thenReturn(progress);
        when(mockEvaluator.supports(QUIZZES_PASSED)).thenReturn(true);
        when(mockEvaluator.evaluate(eq(progress), any())).thenReturn(true);

        listener.onQuizCompleted(new QuizCompletedEvent(USER_ID, 101L, 90, 1, 1));

        assertAll("Verify save and no interaction for inactive achievements",
                () -> verify(achievementProgressService).saveProgress(progress),
                () -> verify(achievementProgressService, never()).getOrCreateAchievementProgress(USER_ID, inactiveAch));
    }

    @Test
    void onReadingCompleted_WhenEvaluatorDoesNotSupport_ShouldNotSave() {
        UserAchievementProgress progress = new UserAchievementProgress();
        progress.setAchievement(activeAch);

        when(achievementService.getAllAchievements()).thenReturn(List.of(activeAch));
        when(achievementProgressService.getOrCreateAchievementProgress(USER_ID, activeAch)).thenReturn(progress);
        when(mockEvaluator.supports(QUIZZES_PASSED)).thenReturn(false);

        listener.onReadingCompleted(new ReadingCompletedEvent(this, 101L, USER_ID));

        verify(achievementProgressService, never()).saveProgress(any());
    }

    @Test
    void onSeasonRanking_ShouldProcessForEveryMemberInEvent() {
        UserAchievementProgress progress = new UserAchievementProgress();
        progress.setAchievement(activeAch);

        when(achievementService.getAllAchievements()).thenReturn(List.of(activeAch));
        when(achievementProgressService.getOrCreateAchievementProgress("u1", activeAch)).thenReturn(progress);
        when(achievementProgressService.getOrCreateAchievementProgress("u2", activeAch)).thenReturn(progress);
        when(mockEvaluator.supports(QUIZZES_PASSED)).thenReturn(true);
        when(mockEvaluator.evaluate(eq(progress), any())).thenReturn(false); // returns false -> no save

        SeasonRankingEvent event = new SeasonRankingEvent(this, List.of("u1", "u2"), "Wibu Elite", "Bronze", 1);
        listener.onSeasonRanking(event);

        assertAll("Verify mock interactions for all users",
                () -> verify(achievementProgressService).getOrCreateAchievementProgress("u1", activeAch),
                () -> verify(achievementProgressService).getOrCreateAchievementProgress("u2", activeAch),
                () -> verify(achievementProgressService, never()).saveProgress(any()));
=======
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

        assertTrue(progress.isUnlocked() && progress.getProgressValue() == 1, ASSERTION_MESSAGE);
>>>>>>> origin/staging
    }
}
