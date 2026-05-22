package id.ac.ui.cs.advprog.yomu.gamification.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.ProgressUpdateRequest;
import id.ac.ui.cs.advprog.yomu.gamification.exception.GamificationException;
import id.ac.ui.cs.advprog.yomu.gamification.mapper.GamificationMapper;
import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;
import id.ac.ui.cs.advprog.yomu.gamification.repository.AchievementRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserAchievementProgressRepository;
import id.ac.ui.cs.advprog.yomu.gamification.service.achievement.AchievementProgressServiceImpl;
import id.ac.ui.cs.advprog.yomu.gamification.validation.GamificationValidator;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AchievementProgressServiceImplTest {

    private static final String ACH_COUNT = "ach-count";

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private UserAchievementProgressRepository userAchievementProgressRepository;

    @Mock
    private GamificationValidator validator;

    @Mock
    private GamificationMapper mapper;

    @InjectMocks
    private AchievementProgressServiceImpl progressService;

    private String username;
    private Achievement achievement;
    private UserAchievementProgress progress;

    @BeforeEach
    void setUp() {
        username = "user-123";
        CountBasedAchievement ach = new CountBasedAchievement();
        ach.setId(ACH_COUNT);
        ach.setName("Novice Reader");
        ach.setMilestone("Read 5 books");
        ach.setMilestoneType("count_based");
        ach.setMilestoneThreshold(5);
        ach.setTier("Bronze");
        achievement = ach;

        progress = new UserAchievementProgress();
        progress.setId("p-123");
        progress.setUsername(username);
        progress.setAchievement(achievement);
        progress.setProgressValue(2);
        progress.setUnlocked(false);
    }

    @Test
    void getOrCreateAchievementProgress_WhenExists_ShouldReturnExisting() {
        when(userAchievementProgressRepository.findByUsernameAndAchievement(username, achievement))
                .thenReturn(Optional.of(progress));

        UserAchievementProgress result = progressService.getOrCreateAchievementProgress(username, achievement);

        assertAll("Verify existing progress returned",
                () -> assertNotNull(result, "Result should not be null"),
                () -> assertEquals("p-123", result.getId(), "Progress ID should match"),
                () -> assertEquals(2, result.getProgressValue(), "Progress value should be 2"));
    }

    @Test
    void getOrCreateAchievementProgress_WhenDoesNotExist_ShouldReturnNew() {
        when(userAchievementProgressRepository.findByUsernameAndAchievement(username, achievement))
                .thenReturn(Optional.empty());

        UserAchievementProgress result = progressService.getOrCreateAchievementProgress(username, achievement);

        assertAll("Verify new progress returned",
                () -> assertNotNull(result, "Result should not be null"),
                () -> assertEquals(username, result.getUsername(), "Username should match"),
                () -> assertEquals(achievement, result.getAchievement(), "Achievement should match"),
                () -> assertEquals(0, result.getProgressValue(), "Progress value should be 0"),
                () -> assertFalse(result.isUnlocked(), "Unlocked flag should be false"));
    }

    @Test
    void upsertAchievementProgress_WhenValidAndUnlocked_ShouldUnlockAndSave() {
        ProgressUpdateRequest request = new ProgressUpdateRequest();
        request.setUsername(username);
        request.setMasterId(ACH_COUNT);
        request.setProgressValue(5);

        doNothing().when(validator).validateMasterId(ACH_COUNT);
        doNothing().when(validator).validateUsername(username);
        when(achievementRepository.findById(ACH_COUNT)).thenReturn(Optional.of(achievement));
        when(userAchievementProgressRepository.findByUsernameAndAchievement(username, achievement))
                .thenReturn(Optional.of(progress));
        when(userAchievementProgressRepository.save(any(UserAchievementProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AchievementProgressResponse expectedResponse = new AchievementProgressResponse(
                ACH_COUNT, "Novice Reader", username, 5, true,
                "Read 5 books", "count_based", 5, 0, "Bronze", null
        );
        when(mapper.toAchievementProgressResponse(any(UserAchievementProgress.class))).thenReturn(expectedResponse);

        AchievementProgressResponse response = progressService.upsertAchievementProgress(request);

        assertAll("Verify upsert, unlock status and mock repository updates",
                () -> assertNotNull(response, "Response should not be null"),
                () -> assertEquals(5, response.progressValue(), "Progress value should be 5"),
                () -> assertTrue(response.unlocked(), "Achievement should be unlocked in response"),
                () -> verify(userAchievementProgressRepository).save(progress),
                () -> assertTrue(progress.isUnlocked(), "Progress entity isUnlocked flag should be true"),
                () -> assertNotNull(progress.getUnlockedAt(), "UnlockedAt timestamp should not be null"));
    }

    @Test
    void upsertAchievementProgress_WhenAlreadyUnlocked_ShouldNotResetUnlockedAt() {
        ProgressUpdateRequest request = new ProgressUpdateRequest();
        request.setUsername(username);
        request.setMasterId(ACH_COUNT);
        request.setProgressValue(6);

        progress.setUnlocked(true);
        LocalDateTime originalUnlockedAt = LocalDateTime.now().minusDays(1);
        progress.setUnlockedAt(originalUnlockedAt);

        doNothing().when(validator).validateMasterId(ACH_COUNT);
        doNothing().when(validator).validateUsername(username);
        when(achievementRepository.findById(ACH_COUNT)).thenReturn(Optional.of(achievement));
        when(userAchievementProgressRepository.findByUsernameAndAchievement(username, achievement))
                .thenReturn(Optional.of(progress));
        when(userAchievementProgressRepository.save(any(UserAchievementProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        progressService.upsertAchievementProgress(request);

        assertEquals(originalUnlockedAt, progress.getUnlockedAt(), "unlockedAt should remain unchanged");
    }

    @Test
    void upsertAchievementProgress_WhenNotFound_ShouldThrowException() {
        ProgressUpdateRequest request = new ProgressUpdateRequest();
        request.setUsername(username);
        request.setMasterId("ach-invalid");
        request.setProgressValue(5);

        doNothing().when(validator).validateMasterId("ach-invalid");
        doNothing().when(validator).validateUsername(username);
        when(achievementRepository.findById("ach-invalid")).thenReturn(Optional.empty());

        assertAll("Verify exception thrown on missing achievement",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> progressService.upsertAchievementProgress(request),
                            "Should throw GamificationException");
                    assertEquals("NOT_FOUND", ex.getErrorCode(), "ErrorCode should be NOT_FOUND");
                });
    }

    @Test
    void getAchievementProgressByUsername_ShouldReturnAllProgress() {
        doNothing().when(validator).validateUsername(username);
        when(achievementRepository.findByActiveTrue()).thenReturn(List.of(achievement));
        when(userAchievementProgressRepository.findByUsernameAndAchievement(username, achievement))
                .thenReturn(Optional.of(progress));

        AchievementProgressResponse expectedResponse = new AchievementProgressResponse(
                ACH_COUNT, "Novice Reader", username, 2, false,
                "Read 5 books", "count_based", 5, 0, "Bronze", null
        );
        when(mapper.toAchievementProgressResponse(progress)).thenReturn(expectedResponse);

        List<AchievementProgressResponse> result = progressService.getAchievementProgressByUsername(username);

        assertAll("Verify returned achievement progress list",
                () -> assertEquals(1, result.size(), "Result list size should be 1"),
                () -> assertEquals(ACH_COUNT, result.get(0).achievementId(), "Achievement ID should match ACH_COUNT"));
    }

    @Test
    void saveProgress_ShouldCallRepository() {
        progressService.saveProgress(progress);
        verify(userAchievementProgressRepository).save(progress);
    }
}
