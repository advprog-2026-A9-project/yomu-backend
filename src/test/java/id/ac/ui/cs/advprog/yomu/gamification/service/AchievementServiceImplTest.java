package id.ac.ui.cs.advprog.yomu.gamification.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementRequest;
import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementResponse;
import id.ac.ui.cs.advprog.yomu.gamification.exception.GamificationException;
import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.AccuracyBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.RankingBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.repository.AchievementRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserAchievementProgressRepository;
import id.ac.ui.cs.advprog.yomu.gamification.service.achievement.AchievementServiceImpl;
import id.ac.ui.cs.advprog.yomu.gamification.validation.GamificationValidator;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AchievementServiceImplTest {

    private static final String ACH_1 = "ach-1";
    private static final String ACH_123 = "ach-123";
    private static final String MILESTONE = "Milestone";
    private static final String GOLD = "Gold";
    private static final String ACCURACY_ABOVE = "accuracy_above";
    private static final String RANKING_ACHIEVED = "ranking_achieved";
    private static final String COUNT_BASED = "count_based";
    private static final String RESPONSE_NOT_NULL = "Response should not be null";
    private static final String THROW_GAMIFICATION_EXCEPTION = "Should throw GamificationException";
    private static final String OLD_NAME = "Old Name";

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private UserAchievementProgressRepository userAchievementProgressRepository;

    @Mock
    private GamificationValidator validator;

    @InjectMocks
    private AchievementServiceImpl achievementService;

    private AchievementRequest accuracyRequest;
    private AchievementRequest rankingRequest;
    private AchievementRequest countRequest;

    @BeforeEach
    void setUp() {
        accuracyRequest = new AchievementRequest();
        accuracyRequest.setName("Master Reader");
        accuracyRequest.setMilestone("Read 5 books with 90% accuracy");
        accuracyRequest.setMilestoneType(ACCURACY_ABOVE);
        accuracyRequest.setMilestoneThreshold(5);
        accuracyRequest.setAccuracyThreshold(90);
        accuracyRequest.setTier(GOLD);

        rankingRequest = new AchievementRequest();
        rankingRequest.setName("Leaderboard Champion");
        rankingRequest.setMilestone("Achieve Gold tier ranking");
        rankingRequest.setMilestoneType(RANKING_ACHIEVED);
        rankingRequest.setMilestoneThreshold(1);
        rankingRequest.setTargetTier(GOLD);
        rankingRequest.setTier(GOLD);

        countRequest = new AchievementRequest();
        countRequest.setName("Novice Reader");
        countRequest.setMilestone("Read 5 books");
        countRequest.setMilestoneType(COUNT_BASED);
        countRequest.setMilestoneThreshold(5);
        countRequest.setTier("Bronze");
    }

    @Test
    void create_WhenAccuracyBased_ShouldSaveAndReturnResponse() {
        doNothing().when(validator).validateAchievementRequest(accuracyRequest);
        when(achievementRepository.findByNameIgnoreCase(accuracyRequest.getName())).thenReturn(Optional.empty());
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(invocation -> {
            Achievement ach = invocation.getArgument(0);
            ach.setId("ach-accuracy");
            ach.setActive(true);
            return ach;
        });
        when(userAchievementProgressRepository.countByAchievement(any(Achievement.class))).thenReturn(10L);

        AchievementResponse response = achievementService.create(accuracyRequest);

        assertAll("Verify accuracy achievement response",
                () -> assertNotNull(response, RESPONSE_NOT_NULL),
                () -> assertEquals("ach-accuracy", response.id(), "ID should match"),
                () -> assertEquals(accuracyRequest.getName(), response.name(), "Name should match"),
                () -> assertEquals(90, response.accuracyThreshold(), "Accuracy threshold should match"),
                () -> assertEquals("GOLD", response.tier(), "Tier should match"),
                () -> assertEquals(10L, response.earnedCount(), "Earned count should match"),
                () -> assertTrue(response.active(), "Active flag should be true"),
                () -> verify(validator).validateAchievementRequest(accuracyRequest));
    }

    @Test
    void create_WhenRankingBased_ShouldSaveAndReturnResponse() {
        doNothing().when(validator).validateAchievementRequest(rankingRequest);
        when(achievementRepository.findByNameIgnoreCase(rankingRequest.getName())).thenReturn(Optional.empty());
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(invocation -> {
            Achievement ach = invocation.getArgument(0);
            ach.setId("ach-ranking");
            ach.setActive(true);
            return ach;
        });
        when(userAchievementProgressRepository.countByAchievement(any(Achievement.class))).thenReturn(5L);

        AchievementResponse response = achievementService.create(rankingRequest);

        assertAll("Verify ranking achievement response",
                () -> assertNotNull(response, RESPONSE_NOT_NULL),
                () -> assertEquals("ach-ranking", response.id(), "ID should match"),
                () -> assertEquals(rankingRequest.getName(), response.name(), "Name should match"),
                () -> assertEquals("GOLD", response.targetTier(), "Target tier should match"),
                () -> assertEquals(5L, response.earnedCount(), "Earned count should match"));
    }

    @Test
    void create_WhenCountBased_ShouldSaveAndReturnResponse() {
        doNothing().when(validator).validateAchievementRequest(countRequest);
        when(achievementRepository.findByNameIgnoreCase(countRequest.getName())).thenReturn(Optional.empty());
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(invocation -> {
            Achievement ach = invocation.getArgument(0);
            ach.setId("ach-count");
            ach.setActive(true);
            return ach;
        });
        when(userAchievementProgressRepository.countByAchievement(any(Achievement.class))).thenReturn(0L);

        AchievementResponse response = achievementService.create(countRequest);

        assertAll("Verify count achievement response",
                () -> assertNotNull(response, RESPONSE_NOT_NULL),
                () -> assertEquals("ach-count", response.id(), "ID should match"),
                () -> assertEquals(countRequest.getName(), response.name(), "Name should match"),
                () -> assertEquals("BRONZE", response.tier(), "Tier should match"),
                () -> assertEquals(0L, response.earnedCount(), "Earned count should match"));
    }

    @Test
    void create_WhenAccuracyBasedWithNullThresholdAndTier_ShouldUseDefaults() {
        AchievementRequest nullRequest = new AchievementRequest();
        nullRequest.setName("Master Reader Nulls");
        nullRequest.setMilestone(MILESTONE);
        nullRequest.setMilestoneType(ACCURACY_ABOVE);

        doNothing().when(validator).validateAchievementRequest(nullRequest);
        when(achievementRepository.findByNameIgnoreCase(nullRequest.getName())).thenReturn(Optional.empty());
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(invocation -> {
            AccuracyBasedAchievement ach = invocation.getArgument(0);
            assertAll("Verify default accuracy threshold and tier",
                    () -> assertEquals(0, ach.getAccuracyThreshold(), "Accuracy threshold should default to 0"),
                    () -> assertNull(ach.getTier(), "Tier should default to null"));
            ach.setId("ach-accuracy-null");
            return ach;
        });

        AchievementResponse response = achievementService.create(nullRequest);
        assertAll("Verify response is returned and save is invoked",
                () -> assertNotNull(response, RESPONSE_NOT_NULL),
                () -> verify(achievementRepository).save(any(Achievement.class)));
    }

    @Test
    void create_WhenRankingBasedWithNullTargetTier_ShouldUseDefaults() {
        AchievementRequest nullRequest = new AchievementRequest();
        nullRequest.setName("Leaderboard Champion Nulls");
        nullRequest.setMilestone(MILESTONE);
        nullRequest.setMilestoneType(RANKING_ACHIEVED);

        doNothing().when(validator).validateAchievementRequest(nullRequest);
        when(achievementRepository.findByNameIgnoreCase(nullRequest.getName())).thenReturn(Optional.empty());
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(invocation -> {
            RankingBasedAchievement ach = invocation.getArgument(0);
            assertNull(ach.getTargetTier(), "Target tier should default to null");
            ach.setId("ach-ranking-null");
            return ach;
        });

        AchievementResponse response = achievementService.create(nullRequest);
        assertAll("Verify response is returned and save is invoked",
                () -> assertNotNull(response, RESPONSE_NOT_NULL),
                () -> verify(achievementRepository).save(any(Achievement.class)));
    }

    @Test
    void create_WhenDuplicateName_ShouldThrowException() {
        doNothing().when(validator).validateAchievementRequest(countRequest);
        CountBasedAchievement existing = new CountBasedAchievement();
        existing.setId("existing-id");
        existing.setName(countRequest.getName());
        when(achievementRepository.findByNameIgnoreCase(countRequest.getName())).thenReturn(Optional.of(existing));

        assertAll("Verify exception on duplicate name during creation",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> achievementService.create(countRequest),
                            THROW_GAMIFICATION_EXCEPTION);
                    assertEquals("DUPLICATE_NAME", ex.getErrorCode(), "ErrorCode should be DUPLICATE_NAME");
                });
    }

    @Test
    void update_WhenValid_ShouldSaveAndReturnResponse() {
        doNothing().when(validator).validateAchievementRequest(countRequest);
        doNothing().when(validator).validateMasterId(ACH_123);

        CountBasedAchievement existing = new CountBasedAchievement();
        existing.setId(ACH_123);
        existing.setName(OLD_NAME);
        existing.setMilestone("Old Milestone");
        existing.setMilestoneType(COUNT_BASED);

        when(achievementRepository.findById(ACH_123)).thenReturn(Optional.of(existing));
        when(achievementRepository.findByNameIgnoreCase(countRequest.getName())).thenReturn(Optional.empty());
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AchievementResponse response = achievementService.update(ACH_123, countRequest);

        assertAll("Verify updated achievement response",
                () -> assertNotNull(response, RESPONSE_NOT_NULL),
                () -> assertEquals(countRequest.getName(), response.name(), "Name should match updated request"),
                () -> assertEquals("BRONZE", response.tier(), "Tier should match updated request"));
    }

    @Test
    void update_WhenAccuracyBasedWithNullThresholdAndTier_ShouldUseDefaults() {
        AchievementRequest nullRequest = new AchievementRequest();
        nullRequest.setName("Master Reader Nulls Updated");
        nullRequest.setMilestone(MILESTONE);
        nullRequest.setMilestoneType(ACCURACY_ABOVE);

        doNothing().when(validator).validateAchievementRequest(nullRequest);
        doNothing().when(validator).validateMasterId(ACH_123);

        AccuracyBasedAchievement existing = new AccuracyBasedAchievement();
        existing.setId(ACH_123);
        existing.setName(OLD_NAME);
        existing.setMilestoneType(ACCURACY_ABOVE);

        when(achievementRepository.findById(ACH_123)).thenReturn(Optional.of(existing));
        when(achievementRepository.findByNameIgnoreCase(nullRequest.getName())).thenReturn(Optional.empty());
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(invocation -> {
            AccuracyBasedAchievement ach = invocation.getArgument(0);
            assertAll("Verify default accuracy threshold and tier on update",
                    () -> assertEquals(0, ach.getAccuracyThreshold(), "Accuracy threshold should default to 0"),
                    () -> assertNull(ach.getTier(), "Tier should default to null"));
            return ach;
        });

        AchievementResponse response = achievementService.update(ACH_123, nullRequest);
        assertAll("Verify response is returned and save is invoked on update",
                () -> assertNotNull(response, RESPONSE_NOT_NULL),
                () -> verify(achievementRepository).save(any(Achievement.class)));
    }

    @Test
    void update_WhenRankingBasedWithNullTargetTier_ShouldUseDefaults() {
        AchievementRequest nullRequest = new AchievementRequest();
        nullRequest.setName("Leaderboard Champion Nulls Updated");
        nullRequest.setMilestone(MILESTONE);
        nullRequest.setMilestoneType(RANKING_ACHIEVED);

        doNothing().when(validator).validateAchievementRequest(nullRequest);
        doNothing().when(validator).validateMasterId(ACH_123);

        RankingBasedAchievement existing = new RankingBasedAchievement();
        existing.setId(ACH_123);
        existing.setName(OLD_NAME);
        existing.setMilestoneType(RANKING_ACHIEVED);

        when(achievementRepository.findById(ACH_123)).thenReturn(Optional.of(existing));
        when(achievementRepository.findByNameIgnoreCase(nullRequest.getName())).thenReturn(Optional.empty());
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(invocation -> {
            RankingBasedAchievement ach = invocation.getArgument(0);
            assertNull(ach.getTargetTier(), "Target tier should default to null");
            return ach;
        });

        AchievementResponse response = achievementService.update(ACH_123, nullRequest);
        assertAll("Verify response is returned and save is invoked on update",
                () -> assertNotNull(response, RESPONSE_NOT_NULL),
                () -> verify(achievementRepository).save(any(Achievement.class)));
    }

    @Test
    void update_WhenNotFound_ShouldThrowException() {
        doNothing().when(validator).validateAchievementRequest(countRequest);
        doNothing().when(validator).validateMasterId(ACH_123);
        when(achievementRepository.findById(ACH_123)).thenReturn(Optional.empty());

        assertAll("Verify exception when updating non-existent achievement",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> achievementService.update(ACH_123, countRequest),
                            THROW_GAMIFICATION_EXCEPTION);
                    assertEquals("NOT_FOUND", ex.getErrorCode(), "ErrorCode should be NOT_FOUND");
                });
    }

    @Test
    void update_WhenTypeChanged_ShouldThrowException() {
        doNothing().when(validator).validateAchievementRequest(accuracyRequest);
        doNothing().when(validator).validateMasterId(ACH_123);

        CountBasedAchievement existing = new CountBasedAchievement();
        existing.setId(ACH_123);
        existing.setName(OLD_NAME);
        existing.setMilestoneType(COUNT_BASED);

        when(achievementRepository.findById(ACH_123)).thenReturn(Optional.of(existing));
        when(achievementRepository.findByNameIgnoreCase(accuracyRequest.getName())).thenReturn(Optional.empty());

        assertAll("Verify exception when updating type of existing achievement",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> achievementService.update(ACH_123, accuracyRequest),
                            THROW_GAMIFICATION_EXCEPTION);
                    assertEquals("INVALID_TYPE_CHANGE", ex.getErrorCode(), "ErrorCode should be INVALID_TYPE_CHANGE");
                });
    }

    @Test
    void update_WhenDuplicateNameForAnotherAchievement_ShouldThrowException() {
        doNothing().when(validator).validateAchievementRequest(countRequest);
        doNothing().when(validator).validateMasterId(ACH_123);

        CountBasedAchievement existing = new CountBasedAchievement();
        existing.setId(ACH_123);
        existing.setName(OLD_NAME);
        existing.setMilestoneType(COUNT_BASED);

        CountBasedAchievement other = new CountBasedAchievement();
        other.setId("ach-other");
        other.setName(countRequest.getName());

        when(achievementRepository.findById(ACH_123)).thenReturn(Optional.of(existing));
        when(achievementRepository.findByNameIgnoreCase(countRequest.getName())).thenReturn(Optional.of(other));

        assertAll("Verify exception when updating to duplicate name of other achievement",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> achievementService.update(ACH_123, countRequest),
                            THROW_GAMIFICATION_EXCEPTION);
                    assertEquals("DUPLICATE_NAME", ex.getErrorCode(), "ErrorCode should be DUPLICATE_NAME");
                });
    }

    @Test
    void delete_WhenExists_ShouldDelete() {
        doNothing().when(validator).validateMasterId(ACH_123);
        CountBasedAchievement existing = new CountBasedAchievement();
        existing.setId(ACH_123);
        when(achievementRepository.findById(ACH_123)).thenReturn(Optional.of(existing));

        achievementService.delete(ACH_123);

        verify(achievementRepository).delete(existing);
    }

    @Test
    void delete_WhenNotFound_ShouldThrowException() {
        doNothing().when(validator).validateMasterId(ACH_123);
        when(achievementRepository.findById(ACH_123)).thenReturn(Optional.empty());

        assertAll("Verify exception when deleting non-existent achievement",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> achievementService.delete(ACH_123),
                            THROW_GAMIFICATION_EXCEPTION);
                    assertEquals("NOT_FOUND", ex.getErrorCode(), "ErrorCode should be NOT_FOUND");
                });
    }

    @Test
    void findAll_ShouldReturnAllResponses() {
        CountBasedAchievement ach1 = new CountBasedAchievement();
        ach1.setId(ACH_1);
        ach1.setName("Name 1");
        ach1.setMilestone("Milestone 1");
        ach1.setMilestoneType(COUNT_BASED);
        ach1.setTier("BRONZE");

        AccuracyBasedAchievement ach2 = new AccuracyBasedAchievement();
        ach2.setId("ach-2");
        ach2.setName("Name 2");
        ach2.setMilestone("Milestone 2");
        ach2.setMilestoneType(ACCURACY_ABOVE);
        ach2.setTier("GOLD");
        ach2.setAccuracyThreshold(90);

        when(achievementRepository.findAll()).thenReturn(List.of(ach1, ach2));
        when(userAchievementProgressRepository.countByAchievement(any(Achievement.class))).thenReturn(0L);

        List<AchievementResponse> responses = achievementService.findAll();

        assertEquals(2, responses.size(), "Responses size should be 2");
    }

    @Test
    void getAllAchievements_ShouldReturnAllAchievements() {
        CountBasedAchievement ach1 = new CountBasedAchievement();
        when(achievementRepository.findAll()).thenReturn(List.of(ach1));

        List<Achievement> result = achievementService.getAllAchievements();

        assertAll("Verify retrieved achievements list",
                () -> assertEquals(1, result.size(), "Result list size should be 1"),
                () -> assertEquals(ach1, result.get(0), "Result element should match ach1"));
    }

    @Test
    void getAchievementsByIds_WhenNullOrEmpty_ShouldReturnEmptyList() {
        assertAll("Verify empty results for null or empty inputs",
                () -> assertTrue(achievementService.getAchievementsByIds(null).isEmpty(), "Null input should return empty list"),
                () -> assertTrue(achievementService.getAchievementsByIds(List.of()).isEmpty(), "Empty list input should return empty list"));
    }

    @Test
    void getAchievementsByIds_WhenIdsProvided_ShouldReturnMatches() {
        CountBasedAchievement ach1 = new CountBasedAchievement();
        ach1.setId(ACH_1);
        when(achievementRepository.findAllById(List.of(ACH_1))).thenReturn(List.of(ach1));

        List<Achievement> result = achievementService.getAchievementsByIds(List.of(ACH_1));

        assertAll("Verify result matches provided IDs",
                () -> assertEquals(1, result.size(), "Result list size should be 1"),
                () -> assertEquals(ACH_1, result.get(0).getId(), "Result element ID should match ACH_1"));
    }
}
