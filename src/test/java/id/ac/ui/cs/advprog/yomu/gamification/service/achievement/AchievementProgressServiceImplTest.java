package id.ac.ui.cs.advprog.yomu.gamification.service.achievement;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import id.ac.ui.cs.advprog.yomu.gamification.validation.GamificationValidator;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"null", "PMD"})
class AchievementProgressServiceImplTest {

    // ── Shared data constants ─────────────────────────────────────────────────
    private static final String USERNAME      = "reader-99";
    private static final String ACH_ID        = "ach-001";
    private static final int    THRESHOLD     = 10;

    // ── Assertion message constants ───────────────────────────────────────────
    private static final String MSG_NOT_NULL    = "Result should not be null";
    private static final String MSG_UNLOCKED    = "Progress should be marked unlocked";
    private static final String MSG_NOT_UNLOCKED = "Progress should not be unlocked below threshold";
    private static final String MSG_SAVED       = "Progress should be saved to repository";
    private static final String MSG_THROW       = "Should throw GamificationException";
    private static final String MSG_CODE_404    = "Error code should be NOT_FOUND";
    private static final String MSG_LIST_SIZE   = "List size should match";

    @Mock private AchievementRepository achievementRepository;
    @Mock private UserAchievementProgressRepository progressRepository;
    @Mock private GamificationValidator validator;
    @Mock private GamificationMapper mapper;

    @InjectMocks
    private AchievementProgressServiceImpl service;

    private Achievement achievement;
    private UserAchievementProgress progress;
    private AchievementProgressResponse dummyResponse;
    private ProgressUpdateRequest request;

    @BeforeEach
    void setUp() {
        achievement = new CountBasedAchievement();
        achievement.setName("Speed Reader");
        achievement.setMilestone("Read 10 books");
        achievement.setMilestoneType("count_based");
        achievement.setMilestoneThreshold(THRESHOLD);

        progress = new UserAchievementProgress();
        progress.setUsername(USERNAME);
        progress.setAchievement(achievement);
        progress.setProgressValue(0);
        progress.setUnlocked(false);

        dummyResponse = new AchievementProgressResponse(
                "prog-1", ACH_ID, USERNAME, 0, false, "Read 10 books", "count_based",
                THRESHOLD, null, null, null);

        request = new ProgressUpdateRequest();
        request.setMasterId(ACH_ID);
        request.setUsername(USERNAME);
        request.setProgressValue(THRESHOLD);
    }

    // ─── getOrCreateAchievementProgress ──────────────────────────────────────

    @Test
    void getOrCreate_WhenExisting_ShouldReturnExisting() {
        when(progressRepository.findByUsernameAndAchievement(USERNAME, achievement))
                .thenReturn(Optional.of(progress));

        UserAchievementProgress result = service.getOrCreateAchievementProgress(USERNAME, achievement);

        assertAll("Verify existing progress is returned",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertEquals(progress, result, "Should return the same existing progress object"));
    }

    @Test
    void getOrCreate_WhenNotExisting_ShouldCreateNew() {
        when(progressRepository.findByUsernameAndAchievement(USERNAME, achievement))
                .thenReturn(Optional.empty());

        UserAchievementProgress result = service.getOrCreateAchievementProgress(USERNAME, achievement);

        assertAll("Verify new progress created with defaults",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertEquals(USERNAME, result.getUsername(), "Username should be set"),
                () -> assertEquals(0, result.getProgressValue(), "Initial progress should be 0"),
                () -> assertEquals(false, result.isUnlocked(), "New progress should not be unlocked"));
    }

    // ─── upsertAchievementProgress ────────────────────────────────────────────

    @Test
    void upsert_WhenAchievementNotFound_ShouldThrow() {
        doNothing().when(validator).validateMasterId(anyString());
        doNothing().when(validator).validateUsername(anyString());
        when(achievementRepository.findById(ACH_ID)).thenReturn(Optional.empty());

        assertAll("Verify exception when achievement not found",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> service.upsertAchievementProgress(request), MSG_THROW);
                    assertEquals("NOT_FOUND", ex.getErrorCode(), MSG_CODE_404);
                });
    }

    @Test
    void upsert_WhenProgressMeetsThreshold_ShouldUnlock() {
        doNothing().when(validator).validateMasterId(anyString());
        doNothing().when(validator).validateUsername(anyString());
        when(achievementRepository.findById(ACH_ID)).thenReturn(Optional.of(achievement));
        when(progressRepository.findByUsernameAndAchievement(any(), any()))
                .thenReturn(Optional.of(progress));
        when(progressRepository.save(any())).thenReturn(progress);
        when(mapper.toAchievementProgressResponse(any())).thenReturn(dummyResponse);

        service.upsertAchievementProgress(request);

        assertAll("Verify progress is unlocked when threshold is met",
                () -> assertEquals(true, progress.isUnlocked(), MSG_UNLOCKED),
                () -> assertNotNull(progress.getUnlockedAt(), "UnlockedAt timestamp should be set"));
    }

    @Test
    void upsert_WhenProgressBelowThreshold_ShouldNotUnlock() {
        request.setProgressValue(THRESHOLD - 1);

        doNothing().when(validator).validateMasterId(anyString());
        doNothing().when(validator).validateUsername(anyString());
        when(achievementRepository.findById(ACH_ID)).thenReturn(Optional.of(achievement));
        when(progressRepository.findByUsernameAndAchievement(any(), any()))
                .thenReturn(Optional.of(progress));
        when(progressRepository.save(any())).thenReturn(progress);
        when(mapper.toAchievementProgressResponse(any())).thenReturn(dummyResponse);

        service.upsertAchievementProgress(request);

        assertAll("Verify progress below threshold remains locked",
                () -> assertEquals(false, progress.isUnlocked(), MSG_NOT_UNLOCKED));
    }

    @Test
    void upsert_ShouldSaveProgress() {
        doNothing().when(validator).validateMasterId(anyString());
        doNothing().when(validator).validateUsername(anyString());
        when(achievementRepository.findById(ACH_ID)).thenReturn(Optional.of(achievement));
        when(progressRepository.findByUsernameAndAchievement(any(), any()))
                .thenReturn(Optional.of(progress));
        when(progressRepository.save(any())).thenReturn(progress);
        when(mapper.toAchievementProgressResponse(any())).thenReturn(dummyResponse);

        AchievementProgressResponse result = service.upsertAchievementProgress(request);

        assertAll("Verify upsert saves and returns response",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> verify(progressRepository).save(progress));
    }

    @Test
    void upsert_WhenAlreadyUnlocked_ShouldNotUnlockAgain() {
        progress.setUnlocked(true);
        java.time.LocalDateTime originalUnlockedAt = java.time.LocalDateTime.now().minusDays(1);
        progress.setUnlockedAt(originalUnlockedAt);

        doNothing().when(validator).validateMasterId(anyString());
        doNothing().when(validator).validateUsername(anyString());
        when(achievementRepository.findById(ACH_ID)).thenReturn(Optional.of(achievement));
        when(progressRepository.findByUsernameAndAchievement(any(), any()))
                .thenReturn(Optional.of(progress));
        when(progressRepository.save(any())).thenReturn(progress);
        when(mapper.toAchievementProgressResponse(any())).thenReturn(dummyResponse);

        service.upsertAchievementProgress(request);

        assertAll("Verify already unlocked progress does not re-unlock",
                () -> assertEquals(true, progress.isUnlocked(), MSG_UNLOCKED),
                () -> assertEquals(originalUnlockedAt, progress.getUnlockedAt(), "UnlockedAt should not change"));
    }

    // ─── getAchievementProgressByUsername ─────────────────────────────────────

    @Test
    void getAchievementProgressByUsername_ShouldReturnMappedList() {
        doNothing().when(validator).validateUsername(anyString());
        when(achievementRepository.findByActiveTrue()).thenReturn(List.of(achievement));
        when(progressRepository.findByUsernameAndAchievement(any(), any()))
                .thenReturn(Optional.of(progress));
        when(mapper.toAchievementProgressResponse(progress)).thenReturn(dummyResponse);

        List<AchievementProgressResponse> result = service.getAchievementProgressByUsername(USERNAME);

        assertAll("Verify progress list is returned for username",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertEquals(1, result.size(), MSG_LIST_SIZE));
    }

    @Test
    void getAchievementProgressByUsername_WhenNoActiveAchievements_ShouldReturnEmpty() {
        doNothing().when(validator).validateUsername(anyString());
        when(achievementRepository.findByActiveTrue()).thenReturn(List.of());

        List<AchievementProgressResponse> result = service.getAchievementProgressByUsername(USERNAME);

        assertAll("Verify empty list when no active achievements",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertEquals(0, result.size(), MSG_LIST_SIZE));
    }

    // ─── saveProgress ─────────────────────────────────────────────────────────

    @Test
    void saveProgress_ShouldDelegateToRepository() {
        when(progressRepository.save(progress)).thenReturn(progress);

        assertAll("Verify saveProgress delegates to repository",
                () -> assertDoesNotThrow(() -> service.saveProgress(progress), MSG_SAVED),
                () -> verify(progressRepository).save(progress));
    }
}
