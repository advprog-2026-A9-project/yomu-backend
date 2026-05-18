package id.ac.ui.cs.advprog.yomu.gamification.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.gamification.event.AllDailyMissionsCompletedEventPublisher;
import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserDailyMissionProgress;
import id.ac.ui.cs.advprog.yomu.gamification.repository.AchievementRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.DailyMissionRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserAchievementProgressRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserDailyMissionProgressRepository;
import id.ac.ui.cs.advprog.yomu.gamification.validation.GamificationValidator;
import id.ac.ui.cs.advprog.yomu.gamification.mapper.GamificationMapper;
import id.ac.ui.cs.advprog.yomu.gamification.strategy.AchievementProgressEvaluator;
import id.ac.ui.cs.advprog.yomu.gamification.strategy.CountBasedAchievementEvaluator;
import id.ac.ui.cs.advprog.yomu.gamification.strategy.AccuracyBasedAchievementEvaluator;
import id.ac.ui.cs.advprog.yomu.gamification.strategy.RankingBasedAchievementEvaluator;
import id.ac.ui.cs.advprog.yomu.gamification.strategy.ClanPromotedAchievementEvaluator;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"null", "unused"})
class ProgressTrackingServiceImplTest {

    private static final String USER_ID = "user-1";
    private static final LocalDate TODAY = LocalDate.now();
    private static final String TRUE = Boolean.TRUE.toString();

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
    private GamificationMapper mapper;

    private ProgressTrackingServiceImpl progressTrackingService;

    private DailyMission readMission;
    private DailyMission skippedMission;
    private Achievement quizAchievement;
    private Achievement skippedAchievement;

    @BeforeEach
    void setUp() {
        readMission = new DailyMission();
        readMission.setId("mission-1");
        readMission.setName("Read One Article");
        readMission.setMilestone("Complete one article today");
        readMission.setMissionType("read_n_articles");
        readMission.setTargetCount(1);
        readMission.setRewardDescription("+10 points");
        readMission.setActiveFrom(TODAY);
        readMission.setActiveUntil(TODAY);
        readMission.setActive(true);

        skippedMission = new DailyMission();
        skippedMission.setId("mission-2");
        skippedMission.setName("Accuracy Mission");
        skippedMission.setMilestone("Score at least 90");
        skippedMission.setMissionType("achieve_accuracy");
        skippedMission.setTargetCount(90);
        skippedMission.setRewardDescription("+20 points");
        skippedMission.setActiveFrom(TODAY);
        skippedMission.setActiveUntil(TODAY);
        skippedMission.setActive(true);

        quizAchievement = new Achievement();
        quizAchievement.setId("achievement-1");
        quizAchievement.setName("Quiz Starter");
        quizAchievement.setMilestone("Pass one quiz");
        quizAchievement.setMilestoneType("quizzes_passed");
        quizAchievement.setMilestoneThreshold(1);
        quizAchievement.setActive(true);

        skippedAchievement = new Achievement();
        skippedAchievement.setId("achievement-2");
        skippedAchievement.setName("Accuracy Pro");
        skippedAchievement.setMilestone("Score at least 90");
        skippedAchievement.setMilestoneType("accuracy_above");
        skippedAchievement.setMilestoneThreshold(90);
        skippedAchievement.setActive(true);

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
        when(userDailyMissionProgressRepository.findByUserIdAndDailyMissionAndProgressDate(USER_ID, readMission, TODAY))
            .thenReturn(Optional.empty());
        when(userAchievementProgressRepository.findByUserIdAndAchievement(USER_ID, quizAchievement))
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
            savedMissionRef.get().getUserId(),
            savedMissionRef.get().getDailyMission().getId(),
            savedMissionRef.get().getProgressDate().toString(),
            String.valueOf(savedMissionRef.get().getProgressValue()),
            String.valueOf(savedMissionRef.get().isCompleted()),
            savedAchievementRef.get().getUserId(),
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

        DailyMission completedMission = new DailyMission();
        completedMission.setId("mission-3");
        completedMission.setName("Quiz Mission");
        completedMission.setMilestone("Complete one quiz");
        completedMission.setMissionType("complete_n_quizzes");
        completedMission.setTargetCount(1);
        completedMission.setRewardDescription("+15 points");
        completedMission.setActiveFrom(TODAY);
        completedMission.setActiveUntil(TODAY);
        completedMission.setActive(true);

        Achievement unlockedAchievement = new Achievement();
        unlockedAchievement.setId("achievement-3");
        unlockedAchievement.setName("Quiz Veteran");
        unlockedAchievement.setMilestone("Pass one quiz");
        unlockedAchievement.setMilestoneType("quizzes_passed");
        unlockedAchievement.setMilestoneThreshold(1);
        unlockedAchievement.setActive(true);

        UserDailyMissionProgress existingMissionProgress = new UserDailyMissionProgress();
        existingMissionProgress.setUserId(USER_ID);
        existingMissionProgress.setDailyMission(completedMission);
        existingMissionProgress.setProgressDate(TODAY);
        existingMissionProgress.setProgressValue(1);
        existingMissionProgress.setCompleted(true);
        existingMissionProgress.setCompletedAt(LocalDateTime.now());

        UserAchievementProgress existingAchievementProgress = new UserAchievementProgress();
        existingAchievementProgress.setUserId(USER_ID);
        existingAchievementProgress.setAchievement(unlockedAchievement);
        existingAchievementProgress.setProgressValue(1);
        existingAchievementProgress.setUnlocked(true);
        existingAchievementProgress.setUnlockedAt(LocalDateTime.now());

        when(dailyMissionRepository.findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(TODAY, TODAY))
            .thenReturn(List.of(completedMission));
        when(achievementRepository.findAll()).thenReturn(List.of(unlockedAchievement));
        when(userDailyMissionProgressRepository.findByUserIdAndDailyMissionAndProgressDate(USER_ID, completedMission, TODAY))
            .thenReturn(Optional.of(existingMissionProgress));
        when(userAchievementProgressRepository.findByUserIdAndAchievement(USER_ID, unlockedAchievement))
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

}