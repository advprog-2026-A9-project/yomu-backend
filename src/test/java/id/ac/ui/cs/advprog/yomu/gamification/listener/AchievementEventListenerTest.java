package id.ac.ui.cs.advprog.yomu.gamification.listener;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;
import id.ac.ui.cs.advprog.yomu.gamification.service.achievement.AchievementProgressService;
import id.ac.ui.cs.advprog.yomu.gamification.service.achievement.AchievementService;
import id.ac.ui.cs.advprog.yomu.gamification.strategy.AchievementProgressEvaluator;
import id.ac.ui.cs.advprog.yomu.gamification.strategy.QuizCompletionContext;
import id.ac.ui.cs.advprog.yomu.reading.event.QuizCompletedEvent;
import id.ac.ui.cs.advprog.yomu.reading.event.ReadingCompletedEvent;
import id.ac.ui.cs.advprog.yomu.social.event.SeasonRankingEvent;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AchievementEventListenerTest {

    private static final String USER_ID = "user-1";
    private static final String QUIZZES_PASSED = "quizzes_passed";

    @Mock
    private AchievementService achievementService;

    @Mock
    private AchievementProgressService achievementProgressService;

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
                List.of(mockEvaluator));

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
    void onQuizCompleted_WhenEvaluatorMatches_SavesProgress() {
        UserAchievementProgress progress = new UserAchievementProgress();
        progress.setAchievement(activeAch);

        when(achievementService.getAllAchievements()).thenReturn(List.of(activeAch, inactiveAch));
        when(achievementProgressService.getOrCreateAchievementProgress(USER_ID, activeAch)).thenReturn(progress);
        when(mockEvaluator.supports(QUIZZES_PASSED)).thenReturn(true);
        when(mockEvaluator.evaluate(eq(progress), any(QuizCompletionContext.class))).thenReturn(true);

        listener.onQuizCompleted(new QuizCompletedEvent(USER_ID, 101L, 90, 1, 1));

        assertAll("Verify quiz achievements are processed",
                () -> verify(achievementProgressService).saveProgress(progress),
                () -> verify(achievementProgressService, never()).getOrCreateAchievementProgress(USER_ID, inactiveAch));
    }

    @Test
    void onReadingCompleted_WhenEvaluatorDoesNotSupport_DoesNotSave() {
        UserAchievementProgress progress = new UserAchievementProgress();
        progress.setAchievement(activeAch);

        when(achievementService.getAllAchievements()).thenReturn(List.of(activeAch));
        when(achievementProgressService.getOrCreateAchievementProgress(USER_ID, activeAch)).thenReturn(progress);
        when(mockEvaluator.supports(QUIZZES_PASSED)).thenReturn(false);

        listener.onReadingCompleted(new ReadingCompletedEvent(this, 101L, USER_ID));

        verify(achievementProgressService, never()).saveProgress(any());
    }

    @Test
    void onSeasonRanking_ProcessesEachMember() {
        UserAchievementProgress progress = new UserAchievementProgress();
        progress.setAchievement(activeAch);

        when(achievementService.getAllAchievements()).thenReturn(List.of(activeAch));
        when(achievementProgressService.getOrCreateAchievementProgress("u1", activeAch)).thenReturn(progress);
        when(achievementProgressService.getOrCreateAchievementProgress("u2", activeAch)).thenReturn(progress);
        when(mockEvaluator.supports(QUIZZES_PASSED)).thenReturn(true);
        when(mockEvaluator.evaluate(eq(progress), any())).thenReturn(false);

        listener.onSeasonRanking(new SeasonRankingEvent(this, List.of("u1", "u2"), "Wibu Elite", "Bronze", 1));

        assertAll("Verify ranking event is processed for every member",
                () -> verify(achievementProgressService).getOrCreateAchievementProgress("u1", activeAch),
                () -> verify(achievementProgressService).getOrCreateAchievementProgress("u2", activeAch),
                () -> verify(achievementProgressService, never()).saveProgress(any()));
    }
}