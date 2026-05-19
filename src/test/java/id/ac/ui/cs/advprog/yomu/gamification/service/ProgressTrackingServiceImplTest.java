package id.ac.ui.cs.advprog.yomu.gamification.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import id.ac.ui.cs.advprog.yomu.gamification.event.AllDailyMissionsCompletedEventPublisher;
import id.ac.ui.cs.advprog.yomu.gamification.mapper.GamificationMapper;
import id.ac.ui.cs.advprog.yomu.gamification.model.AccuracyBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.AccuracyDailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedDailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserDailyMissionProgress;
import id.ac.ui.cs.advprog.yomu.gamification.repository.AchievementRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.DailyMissionRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserAchievementProgressRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserDailyMissionProgressRepository;
import id.ac.ui.cs.advprog.yomu.gamification.strategy.AccuracyBasedAchievementEvaluator;
import id.ac.ui.cs.advprog.yomu.gamification.strategy.AchievementProgressEvaluator;
import id.ac.ui.cs.advprog.yomu.gamification.strategy.ClanPromotedAchievementEvaluator;
import id.ac.ui.cs.advprog.yomu.gamification.strategy.CountBasedAchievementEvaluator;
import id.ac.ui.cs.advprog.yomu.gamification.strategy.RankingBasedAchievementEvaluator;
import id.ac.ui.cs.advprog.yomu.gamification.validation.GamificationValidator;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"null", "unused"})
class ProgressTrackingServiceImplTest {

    private static final String USER_ID = "user-1";
    private static final LocalDate TODAY = LocalDate.now();
    private static final String TRUE = Boolean.TRUE.toString();
    private static final String ACHIEVE_ACCURACY = "achieve_accuracy";

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private DailyMissionRepository dailyMissionRepository;

    @Mock
    private UserAchievementProgressRepository userAchievementProgressRepository;

    @Mock
    private UserDailyMissionProgressRepository userDailyMissionProgressRepository;

    @Mock
    private GamificationValidator validator;

    @Mock
    private AllDailyMissionsCompletedEventPublisher allDailyMissionsCompletedEventPublisher;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private GamificationMapper mapper;

    private ProgressTrackingServiceImpl progressTrackingService;

    private DailyMission readMission;
    private DailyMission skippedMission;
    private Achievement quizAchievement;
    private Achievement skippedAchievement;
    private AccuracyBasedAchievement multiQuizAccAchievement;

    @BeforeEach
    void setUp() {
        CountBasedDailyMission cMission = new CountBasedDailyMission();
        cMission.setId("mission-1");
        cMission.setName("Read One Article");
        cMission.setMilestone("Complete one article today");
        cMission.setMissionType("read_n_articles");
        cMission.setTargetCount(1);
        cMission.setRewardScore(10);
        cMission.setActiveFrom(TODAY);
        cMission.setActiveUntil(TODAY);
        cMission.setActive(true);
        readMission = cMission;

        AccuracyDailyMission aMission = new AccuracyDailyMission();
        aMission.setId("mission-2");
        aMission.setName("Accuracy Mission");
        aMission.setMilestone("Score at least 90");
        aMission.setMissionType(ACHIEVE_ACCURACY);
        aMission.setAccuracyThreshold(90);
        aMission.setRequiredCount(1);
        aMission.setRewardScore(20);
        aMission.setActiveFrom(TODAY);
        aMission.setActiveUntil(TODAY);
        aMission.setActive(true);
        skippedMission = aMission;

        CountBasedAchievement countAch = new CountBasedAchievement();
        countAch.setId("achievement-1");
        countAch.setName("Quiz Starter");
        countAch.setMilestone("Pass one quiz");
        countAch.setMilestoneType("quizzes_passed");
        countAch.setMilestoneThreshold(1);
        countAch.setActive(true);
        quizAchievement = countAch;

        AccuracyBasedAchievement accAch = new AccuracyBasedAchievement();
        accAch.setId("achievement-2");
        accAch.setName("Accuracy Pro");
        accAch.setMilestone("Score at least 90");
        accAch.setMilestoneType("accuracy_above");
        accAch.setAccuracyThreshold(90);
        accAch.setMilestoneThreshold(1);
        accAch.setActive(true);
        skippedAchievement = accAch;

        List<AchievementProgressEvaluator> achievementEvaluators = List.of(
            new CountBasedAchievementEvaluator(),
            new AccuracyBasedAchievementEvaluator(),
            new RankingBasedAchievementEvaluator(),
            new ClanPromotedAchievementEvaluator()
        );

        progressTrackingService = new ProgressTrackingServiceImpl(
            achievementRepository,
            dailyMissionRepository,
            userAchievementProgressRepository,
            userDailyMissionProgressRepository,
            validator,
            mapper,
            allDailyMissionsCompletedEventPublisher,
            eventPublisher,
            achievementEvaluators
        );
    }

    @Test
    void handleQuizCompletion_ShouldUpdateMatchingMissionAndAchievement() {
        AtomicReference<UserDailyMissionProgress> savedMissionRef = new AtomicReference<>();
        AtomicReference<UserAchievementProgress> savedAchievementRef = new AtomicReference<>();

        when(dailyMissionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(TODAY, TODAY))
            .thenReturn(List.of(readMission, skippedMission));
        when(achievementRepository.findAll()).thenReturn(List.of(quizAchievement, skippedAchievement));
        when(userDailyMissionProgressRepository.findByUsernameAndDailyMissionAndProgressDate(USER_ID, readMission, TODAY))
            .thenReturn(Optional.empty());
        when(userAchievementProgressRepository.findByUsernameAndAchievement(USER_ID, quizAchievement))
            .thenReturn(Optional.empty());
        when(userDailyMissionProgressRepository.save(any(UserDailyMissionProgress.class)))
            .thenAnswer(invocation -> {
                UserDailyMissionProgress saved = invocation.getArgument(0);
                savedMissionRef.set(saved);
                return saved;
            });
        when(userAchievementProgressRepository.save(any(UserAchievementProgress.class)))
            .thenAnswer(invocation -> {
                UserAchievementProgress saved = invocation.getArgument(0);
                savedAchievementRef.set(saved);
                return saved;
            });

        progressTrackingService.handleQuizCompletion(USER_ID, 82);

        String expectedSignature = String.join("|",
            USER_ID,
            readMission.getId(),
            TODAY.toString(),
            "1",
            TRUE,
            USER_ID,
            quizAchievement.getId(),
            "1",
            TRUE
        );
        String actualSignature = String.join("|",
            savedMissionRef.get().getUsername(),
            savedMissionRef.get().getDailyMission().getId(),
            savedMissionRef.get().getProgressDate().toString(),
            String.valueOf(savedMissionRef.get().getProgressValue()),
            String.valueOf(savedMissionRef.get().isCompleted()),
            savedAchievementRef.get().getUsername(),
            savedAchievementRef.get().getAchievement().getId(),
            String.valueOf(savedAchievementRef.get().getProgressValue()),
            String.valueOf(savedAchievementRef.get().isUnlocked())
        );

        assertEquals(expectedSignature, actualSignature, "Quiz completion should update the matching mission and achievement");
    }

    @Test
    void handleQuizCompletion_ShouldSkipAlreadyFinishedProgress() {
        AtomicReference<Boolean> missionSaved = new AtomicReference<>(false);
        AtomicReference<Boolean> achievementSaved = new AtomicReference<>(false);

        CountBasedDailyMission completedMission = new CountBasedDailyMission();
        completedMission.setId("mission-3");
        completedMission.setName("Quiz Mission");
        completedMission.setMilestone("Complete one quiz");
        completedMission.setMissionType("complete_n_quizzes");
        completedMission.setTargetCount(1);
        completedMission.setRewardScore(15);
        completedMission.setActiveFrom(TODAY);
        completedMission.setActiveUntil(TODAY);
        completedMission.setActive(true);

        CountBasedAchievement unlockedAchievement = new CountBasedAchievement();
        unlockedAchievement.setId("achievement-3");
        unlockedAchievement.setName("Quiz Veteran");
        unlockedAchievement.setMilestone("Pass one quiz");
        unlockedAchievement.setMilestoneType("quizzes_passed");
        unlockedAchievement.setMilestoneThreshold(1);
        unlockedAchievement.setActive(true);

        UserDailyMissionProgress existingMissionProgress = new UserDailyMissionProgress();
        existingMissionProgress.setUsername(USER_ID);
        existingMissionProgress.setDailyMission(completedMission);
        existingMissionProgress.setProgressDate(TODAY);
        existingMissionProgress.setProgressValue(1);
        existingMissionProgress.setCompleted(true);
        existingMissionProgress.setCompletedAt(LocalDateTime.now());

        UserAchievementProgress existingAchievementProgress = new UserAchievementProgress();
        existingAchievementProgress.setUsername(USER_ID);
        existingAchievementProgress.setAchievement(unlockedAchievement);
        existingAchievementProgress.setProgressValue(1);
        existingAchievementProgress.setUnlocked(true);
        existingAchievementProgress.setUnlockedAt(LocalDateTime.now());

        when(dailyMissionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(TODAY, TODAY))
            .thenReturn(List.of(completedMission));
        when(achievementRepository.findAll()).thenReturn(List.of(unlockedAchievement));
        when(userDailyMissionProgressRepository.findByUsernameAndDailyMissionAndProgressDate(USER_ID, completedMission, TODAY))
            .thenReturn(Optional.of(existingMissionProgress));
        when(userAchievementProgressRepository.findByUsernameAndAchievement(USER_ID, unlockedAchievement))
            .thenReturn(Optional.of(existingAchievementProgress));
        lenient().when(userDailyMissionProgressRepository.save(any(UserDailyMissionProgress.class)))
            .thenAnswer(invocation -> {
                missionSaved.set(true);
                return invocation.getArgument(0);
            });
        lenient().when(userAchievementProgressRepository.save(any(UserAchievementProgress.class)))
            .thenAnswer(invocation -> {
                achievementSaved.set(true);
                return invocation.getArgument(0);
            });

        progressTrackingService.handleQuizCompletion(USER_ID, 95);

        String expectedSignature = String.join("|", "1", TRUE, "1", TRUE, "false", "false");
        String actualSignature = String.join("|",
            String.valueOf(existingMissionProgress.getProgressValue()),
            String.valueOf(existingMissionProgress.isCompleted()),
            String.valueOf(existingAchievementProgress.getProgressValue()),
            String.valueOf(existingAchievementProgress.isUnlocked()),
            String.valueOf(missionSaved.get()),
            String.valueOf(achievementSaved.get())
        );

        assertEquals(expectedSignature, actualSignature, "Already completed progress should remain unchanged");
    }

    @Test
    void handleQuizCompletion_WithAccuracyMission_ShouldSetProgressToAccuracyScoreAndComplete() {
        // helper used by split assertions below
        AtomicReference<UserDailyMissionProgress> savedMissionRef = runAccuracyMissionAndReturnSaved();
        // single-assert smoke check to ensure helper worked
        assertNotNull(savedMissionRef.get(), "Accuracy mission progress should be saved");
    }

    @Test
    void handleQuizCompletion_WithAccuracyMission_ProgressValueIs100() {
        AtomicReference<UserDailyMissionProgress> savedMissionRef = runAccuracyMissionAndReturnSaved();
        assertEquals(100, savedMissionRef.get().getProgressValue(), "Progress value should be 100% accuracy");
    }

    @Test
    void handleQuizCompletion_WithAccuracyMission_IsCompleted() {
        AtomicReference<UserDailyMissionProgress> savedMissionRef = runAccuracyMissionAndReturnSaved();
        assertTrue(savedMissionRef.get().isCompleted(), "Accuracy mission should be completed");
    }

    private AtomicReference<UserDailyMissionProgress> runAccuracyMissionAndReturnSaved() {
        AtomicReference<UserDailyMissionProgress> savedMissionRef = new AtomicReference<>();

        AccuracyDailyMission accuracyMission = new AccuracyDailyMission();
        accuracyMission.setId("mission-accuracy-1");
        accuracyMission.setName("Perfect Score");
        accuracyMission.setMilestone("Achieve 100% accuracy");
        accuracyMission.setMissionType("achieve_accuracy");
        accuracyMission.setAccuracyThreshold(100);
        accuracyMission.setRequiredCount(1);
        accuracyMission.setActiveFrom(TODAY);
        accuracyMission.setActiveUntil(TODAY);
        accuracyMission.setActive(true);

        when(dailyMissionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(TODAY, TODAY))
            .thenReturn(List.of(accuracyMission));
        when(achievementRepository.findAll()).thenReturn(List.of());
        when(userDailyMissionProgressRepository.findByUsernameAndDailyMissionAndProgressDate(USER_ID, accuracyMission, TODAY))
            .thenReturn(Optional.empty());

        when(userDailyMissionProgressRepository.save(any(UserDailyMissionProgress.class)))
            .thenAnswer(invocation -> {
                UserDailyMissionProgress saved = invocation.getArgument(0);
                savedMissionRef.set(saved);
                return saved;
            });

        progressTrackingService.handleQuizCompletion(USER_ID, 100);
        return savedMissionRef;
    }

    @Test
    void handleQuizCompletion_WithMultiQuizAccuracyMission_ShouldStepProgressCorrectly() {
        // split into two tests below; keep this method as a compatibility wrapper
        AtomicReference<UserDailyMissionProgress> savedMissionRef = runMultiQuizAccuracy_FirstCompletionAndReturnSaved();
        assertEquals(35, savedMissionRef.get().getProgressValue(), "First quiz should set progress to 35");
    }

    @Test
    void handleQuizCompletion_WithMultiQuizAccuracyMission_FirstCompletionSetsProgress() {
        AtomicReference<UserDailyMissionProgress> savedMissionRef = runMultiQuizAccuracy_FirstCompletionAndReturnSaved();
        assertEquals(35, savedMissionRef.get().getProgressValue(), "First quiz should set progress to 35");
    }

    @Test
    void handleQuizCompletion_WithMultiQuizAccuracyMission_SecondCompletionCompletes() {
        AccuracyDailyMission multiAccuracyMission = new AccuracyDailyMission();
        multiAccuracyMission.setId("mission-multi-accuracy-1");
        multiAccuracyMission.setName("Consistent Scholar");
        multiAccuracyMission.setMilestone("Achieve at least 70% accuracy in 2 quizzes");
        multiAccuracyMission.setMissionType(ACHIEVE_ACCURACY);
        multiAccuracyMission.setAccuracyThreshold(70);
        multiAccuracyMission.setRequiredCount(2);
        multiAccuracyMission.setActiveFrom(TODAY);
        multiAccuracyMission.setActiveUntil(TODAY);
        multiAccuracyMission.setActive(true);

        // prepare existing progress at 35
        UserDailyMissionProgress existingProgress = new UserDailyMissionProgress();
        existingProgress.setUsername(USER_ID);
        existingProgress.setDailyMission(multiAccuracyMission);
        existingProgress.setProgressDate(TODAY);
        existingProgress.setProgressValue(35);
        existingProgress.setCompleted(false);

        when(dailyMissionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(TODAY, TODAY))
            .thenReturn(List.of(multiAccuracyMission));
        when(achievementRepository.findAll()).thenReturn(List.of());
        when(userDailyMissionProgressRepository.findByUsernameAndDailyMissionAndProgressDate(USER_ID, multiAccuracyMission, TODAY))
            .thenReturn(Optional.of(existingProgress));

        progressTrackingService.handleQuizCompletion(USER_ID, 80);

        String combined = String.join("|", String.valueOf(existingProgress.getProgressValue()), String.valueOf(existingProgress.isCompleted()));
        assertEquals("70|true", combined, "Second quiz should set progress to 70 and mark as completed");
    }

    private AtomicReference<UserDailyMissionProgress> runMultiQuizAccuracy_FirstCompletionAndReturnSaved() {
        AtomicReference<UserDailyMissionProgress> savedMissionRef = new AtomicReference<>();

        AccuracyDailyMission multiAccuracyMission = new AccuracyDailyMission();
        multiAccuracyMission.setId("mission-multi-accuracy-1");
        multiAccuracyMission.setName("Consistent Scholar");
        multiAccuracyMission.setMilestone("Achieve at least 70% accuracy in 2 quizzes");
        multiAccuracyMission.setMissionType(ACHIEVE_ACCURACY);
        multiAccuracyMission.setAccuracyThreshold(70);
        multiAccuracyMission.setRequiredCount(2);
        multiAccuracyMission.setActiveFrom(TODAY);
        multiAccuracyMission.setActiveUntil(TODAY);
        multiAccuracyMission.setActive(true);

        when(dailyMissionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(TODAY, TODAY))
            .thenReturn(List.of(multiAccuracyMission));
        when(achievementRepository.findAll()).thenReturn(List.of());
        when(userDailyMissionProgressRepository.findByUsernameAndDailyMissionAndProgressDate(USER_ID, multiAccuracyMission, TODAY))
            .thenReturn(Optional.empty());

        when(userDailyMissionProgressRepository.save(any(UserDailyMissionProgress.class)))
            .thenAnswer(invocation -> {
                UserDailyMissionProgress saved = invocation.getArgument(0);
                savedMissionRef.set(saved);
                return saved;
            });

        progressTrackingService.handleQuizCompletion(USER_ID, 75);
        return savedMissionRef;
    }

    private AtomicReference<UserAchievementProgress> runAccAchievementInitialSave() {
        AtomicReference<UserAchievementProgress> savedAchievementRef = new AtomicReference<>();

        multiQuizAccAchievement = new AccuracyBasedAchievement();
        multiQuizAccAchievement.setId("ach-multi-accuracy-1");
        multiQuizAccAchievement.setName("Flawless Master");
        multiQuizAccAchievement.setMilestone("Get 100% accuracy 10 times");
        multiQuizAccAchievement.setMilestoneType("accuracy_above");
        multiQuizAccAchievement.setAccuracyThreshold(100);
        multiQuizAccAchievement.setMilestoneThreshold(10);
        multiQuizAccAchievement.setActive(true);

        when(dailyMissionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(TODAY, TODAY))
            .thenReturn(List.of());
        when(achievementRepository.findAll()).thenReturn(List.of(multiQuizAccAchievement));
        when(userAchievementProgressRepository.findByUsernameAndAchievement(USER_ID, multiQuizAccAchievement))
            .thenReturn(Optional.empty());

        when(userAchievementProgressRepository.save(any(UserAchievementProgress.class)))
            .thenAnswer(invocation -> {
                UserAchievementProgress saved = invocation.getArgument(0);
                savedAchievementRef.set(saved);
                return saved;
            });

        // 1st quiz: 100% accuracy -> progress incremented to 1
        progressTrackingService.handleQuizCompletion(USER_ID, 100);
        return savedAchievementRef;
    }

    @Test
    void handleQuizCompletion_WithMultiQuizAccuracyAchievement_SavesProgress() {
        AtomicReference<UserAchievementProgress> saved = runAccAchievementInitialSave();
        assertNotNull(saved.get(), "Achievement progress should be saved");
    }

    @Test
    void handleQuizCompletion_WithMultiQuizAccuracyAchievement_FirstQuizSetsProgressTo1() {
        AtomicReference<UserAchievementProgress> saved = runAccAchievementInitialSave();
        assertEquals(1, saved.get().getProgressValue(), "Progress value should be incremented to 1");
    }

    @Test
    void handleQuizCompletion_WithMultiQuizAccuracyAchievement_FirstQuizDoesNotUnlock() {
        AtomicReference<UserAchievementProgress> saved = runAccAchievementInitialSave();
        assertFalse(saved.get().isUnlocked(), "Achievement should not be unlocked yet");
    }

    @Test
    void handleQuizCompletion_WithMultiQuizAccuracyAchievement_Sub100DoesNotIncrement() {
        AtomicReference<UserAchievementProgress> saved = runAccAchievementInitialSave();
        UserAchievementProgress existingProgress = saved.get();

        when(userAchievementProgressRepository.findByUsernameAndAchievement(USER_ID, multiQuizAccAchievement))
            .thenReturn(Optional.of(existingProgress));

        progressTrackingService.handleQuizCompletion(USER_ID, 90);
        assertEquals(1, existingProgress.getProgressValue(), "Progress should not increment for sub-100% accuracy");
    }

    @Test
    void handleQuizCompletion_WithMultiQuizAccuracyAchievement_Second100Increments() {
        AtomicReference<UserAchievementProgress> saved = runAccAchievementInitialSave();
        UserAchievementProgress existingProgress = saved.get();

        when(userAchievementProgressRepository.findByUsernameAndAchievement(USER_ID, multiQuizAccAchievement))
            .thenReturn(Optional.of(existingProgress));

        progressTrackingService.handleQuizCompletion(USER_ID, 100);
        assertEquals(2, existingProgress.getProgressValue(), "Progress should increment to 2 on next 100% accuracy");
    }
}