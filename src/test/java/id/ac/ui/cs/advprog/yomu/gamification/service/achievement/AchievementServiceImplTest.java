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

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementRequest;
import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementResponse;
import id.ac.ui.cs.advprog.yomu.gamification.exception.GamificationException;
import id.ac.ui.cs.advprog.yomu.gamification.model.AccuracyBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.RankingBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.repository.AchievementRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserAchievementProgressRepository;
import id.ac.ui.cs.advprog.yomu.gamification.validation.GamificationValidator;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD")
class AchievementServiceImplTest {

    // ── Shared data constants ─────────────────────────────────────────────────
    private static final String ACH_ID          = "ach-001";
    private static final String ACH_NAME        = "Speed Reader";
    private static final String ACH_MILESTONE   = "Read 10 books";
    private static final String TYPE_COUNT      = "count_based";
    private static final String TYPE_ACCURACY   = "accuracy_above";
    private static final String TYPE_RANKING    = "ranking_achieved";
    private static final String TIER_SILVER     = "SILVER";

    // ── Assertion message constants ───────────────────────────────────────────
    private static final String MSG_NOT_NULL    = "Response should not be null";
    private static final String MSG_SAVED       = "Achievement should be saved to repository";
    private static final String MSG_DELETED     = "Achievement should be deleted from repository";
    private static final String MSG_THROW       = "Should throw GamificationException";
    private static final String MSG_CODE_DUP    = "Error code should be DUPLICATE_NAME";
    private static final String MSG_CODE_404    = "Error code should be NOT_FOUND";
    private static final String MSG_CODE_TYPE   = "Error code should be INVALID_TYPE_CHANGE";
    private static final String MSG_LIST_SIZE   = "List size should match";

    @Mock private AchievementRepository achievementRepository;
    @Mock private UserAchievementProgressRepository userAchievementProgressRepository;
    @Mock private GamificationValidator validator;

    @InjectMocks
    private AchievementServiceImpl service;

    private AchievementRequest countRequest;
    private AchievementRequest accuracyRequest;
    private AchievementRequest rankingRequest;
    private CountBasedAchievement savedCount;
    private AccuracyBasedAchievement savedAccuracy;
    private RankingBasedAchievement savedRanking;

    @BeforeEach
    void setUp() {
        countRequest = new AchievementRequest();
        countRequest.setName(ACH_NAME);
        countRequest.setMilestone(ACH_MILESTONE);
        countRequest.setMilestoneType(TYPE_COUNT);
        countRequest.setMilestoneThreshold(10);

        accuracyRequest = new AchievementRequest();
        accuracyRequest.setName(ACH_NAME + " Accuracy");
        accuracyRequest.setMilestone(ACH_MILESTONE);
        accuracyRequest.setMilestoneType(TYPE_ACCURACY);
        accuracyRequest.setMilestoneThreshold(5);
        accuracyRequest.setAccuracyThreshold(90);

        rankingRequest = new AchievementRequest();
        rankingRequest.setName(ACH_NAME + " Ranking");
        rankingRequest.setMilestone(ACH_MILESTONE);
        rankingRequest.setMilestoneType(TYPE_RANKING);
        rankingRequest.setMilestoneThreshold(1);
        rankingRequest.setTargetTier(TIER_SILVER);

        savedCount = new CountBasedAchievement();
        savedCount.setName(ACH_NAME);
        savedCount.setMilestone(ACH_MILESTONE);
        savedCount.setMilestoneType(TYPE_COUNT);
        savedCount.setMilestoneThreshold(10);

        savedAccuracy = new AccuracyBasedAchievement();
        savedAccuracy.setName(ACH_NAME + " Accuracy");
        savedAccuracy.setMilestone(ACH_MILESTONE);
        savedAccuracy.setMilestoneType(TYPE_ACCURACY);
        savedAccuracy.setMilestoneThreshold(5);
        savedAccuracy.setAccuracyThreshold(90);

        savedRanking = new RankingBasedAchievement();
        savedRanking.setName(ACH_NAME + " Ranking");
        savedRanking.setMilestone(ACH_MILESTONE);
        savedRanking.setMilestoneType(TYPE_RANKING);
        savedRanking.setMilestoneThreshold(1);
        savedRanking.setTargetTier(TIER_SILVER);
    }

    // ─── create ──────────────────────────────────────────────────────────────

    @Test
    void create_WhenCountBased_ShouldSaveAndReturn() {
        doNothing().when(validator).validateAchievementRequest(any());
        when(achievementRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(achievementRepository.save(any())).thenReturn(savedCount);
        when(userAchievementProgressRepository.countByAchievement(any())).thenReturn(0L);

        AchievementResponse result = service.create(countRequest);

        assertAll("Verify count-based achievement is created",
                () -> assertNotNull(result, MSG_NOT_NULL),
            () -> verify(achievementRepository).save(any()));
    }

    @Test
    void create_WhenAccuracyBased_ShouldSaveAndReturn() {
        doNothing().when(validator).validateAchievementRequest(any());
        when(achievementRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(achievementRepository.save(any())).thenReturn(savedAccuracy);
        when(userAchievementProgressRepository.countByAchievement(any())).thenReturn(0L);

        AchievementResponse result = service.create(accuracyRequest);

        assertAll("Verify accuracy-based achievement is created",
                () -> assertNotNull(result, MSG_NOT_NULL),
            () -> verify(achievementRepository).save(any()));
    }

    @Test
    void create_WhenRankingBased_ShouldSaveAndReturn() {
        doNothing().when(validator).validateAchievementRequest(any());
        when(achievementRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(achievementRepository.save(any())).thenReturn(savedRanking);
        when(userAchievementProgressRepository.countByAchievement(any())).thenReturn(0L);

        AchievementResponse result = service.create(rankingRequest);

        assertAll("Verify ranking-based achievement is created",
                () -> assertNotNull(result, MSG_NOT_NULL),
            () -> verify(achievementRepository).save(any()));
    }

    @Test
    void create_WhenDuplicateName_ShouldThrow() {
        doNothing().when(validator).validateAchievementRequest(any());
        when(achievementRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(savedCount));

        assertAll("Verify duplicate name throws on create",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> service.create(countRequest), MSG_THROW);
                    assertEquals("DUPLICATE_NAME", ex.getErrorCode(), MSG_CODE_DUP);
                });
    }

    @Test
    void create_WhenAccuracyBasedWithNullThreshold_ShouldSaveAndReturn() {
        accuracyRequest.setAccuracyThreshold(null);
        doNothing().when(validator).validateAchievementRequest(any());
        when(achievementRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(achievementRepository.save(any())).thenReturn(savedAccuracy);
        when(userAchievementProgressRepository.countByAchievement(any())).thenReturn(0L);

        AchievementResponse result = service.create(accuracyRequest);

        assertAll("Verify accuracy-based achievement with null threshold is created",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> verify(achievementRepository).save(any()));
    }

    @Test
    void create_WhenRankingBasedWithNullTargetTier_ShouldSaveAndReturn() {
        rankingRequest.setTargetTier(null);
        doNothing().when(validator).validateAchievementRequest(any());
        when(achievementRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(achievementRepository.save(any())).thenReturn(savedRanking);
        when(userAchievementProgressRepository.countByAchievement(any())).thenReturn(0L);

        AchievementResponse result = service.create(rankingRequest);

        assertAll("Verify ranking-based achievement with null target tier is created",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> verify(achievementRepository).save(any()));
    }

    @Test
    void create_WhenRankingBasedWithBlankTargetTier_ShouldSaveAndReturn() {
        rankingRequest.setTargetTier("   ");
        doNothing().when(validator).validateAchievementRequest(any());
        when(achievementRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(achievementRepository.save(any())).thenReturn(savedRanking);
        when(userAchievementProgressRepository.countByAchievement(any())).thenReturn(0L);

        AchievementResponse result = service.create(rankingRequest);

        assertAll("Verify ranking-based achievement with blank target tier is created",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> verify(achievementRepository).save(any()));
    }

    @Test
    void create_WhenTierIsBlank_ShouldSaveAndReturn() {
        countRequest.setTier("   ");
        doNothing().when(validator).validateAchievementRequest(any());
        when(achievementRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(achievementRepository.save(any())).thenReturn(savedCount);
        when(userAchievementProgressRepository.countByAchievement(any())).thenReturn(0L);

        AchievementResponse result = service.create(countRequest);

        assertAll("Verify achievement with blank tier is created",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> verify(achievementRepository).save(any()));
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Test
    void update_WhenNotFound_ShouldThrow() {
        doNothing().when(validator).validateAchievementRequest(any());
        doNothing().when(validator).validateMasterId(anyString());
        when(achievementRepository.findById(ACH_ID)).thenReturn(Optional.empty());

        assertAll("Verify exception when updating non-existent achievement",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> service.update(ACH_ID, countRequest), MSG_THROW);
                    assertEquals("NOT_FOUND", ex.getErrorCode(), MSG_CODE_404);
                });
    }

    @Test
    void update_WhenTypeChanges_ShouldThrow() {
        // existing is accuracy; request is count → invalid
        doNothing().when(validator).validateAchievementRequest(any());
        doNothing().when(validator).validateMasterId(anyString());
        when(achievementRepository.findById(ACH_ID)).thenReturn(Optional.of(savedAccuracy));
        when(achievementRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());

        assertAll("Verify type change throws on update",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> service.update(ACH_ID, countRequest), MSG_THROW);
                    assertEquals("INVALID_TYPE_CHANGE", ex.getErrorCode(), MSG_CODE_TYPE);
                });
    }

    @Test
    void update_WhenDuplicateName_ShouldThrow() {
        Achievement other = new CountBasedAchievement();
        other.setId("other-achievement");
        other.setName("Other Achievement");
        doNothing().when(validator).validateAchievementRequest(any());
        doNothing().when(validator).validateMasterId(anyString());
        when(achievementRepository.findById(ACH_ID)).thenReturn(Optional.of(savedCount));
        when(achievementRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.of(other));

        assertAll("Verify duplicate name on update throws",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> service.update(ACH_ID, countRequest), MSG_THROW);
                    assertEquals("DUPLICATE_NAME", ex.getErrorCode(), MSG_CODE_DUP);
                });
    }

    @Test
    void update_WhenValid_ShouldSaveAndReturn() {
        doNothing().when(validator).validateAchievementRequest(any());
        doNothing().when(validator).validateMasterId(anyString());
        when(achievementRepository.findById(ACH_ID)).thenReturn(Optional.of(savedCount));
        when(achievementRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(achievementRepository.save(any())).thenReturn(savedCount);
        when(userAchievementProgressRepository.countByAchievement(any())).thenReturn(0L);

        AchievementResponse result = service.update(ACH_ID, countRequest);

        assertAll("Verify valid update saves and returns response",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> verify(achievementRepository).save(savedCount));
    }

    @Test
    void update_WhenAccuracyBasedValid_ShouldSaveAndReturn() {
        doNothing().when(validator).validateAchievementRequest(any());
        doNothing().when(validator).validateMasterId(anyString());
        when(achievementRepository.findById(ACH_ID)).thenReturn(Optional.of(savedAccuracy));
        when(achievementRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(achievementRepository.save(any())).thenReturn(savedAccuracy);
        when(userAchievementProgressRepository.countByAchievement(any())).thenReturn(0L);

        AchievementResponse result = service.update(ACH_ID, accuracyRequest);

        assertAll("Verify valid accuracy update saves and returns response",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> verify(achievementRepository).save(savedAccuracy));
    }

    @Test
    void update_WhenAccuracyBasedValidWithNullThreshold_ShouldSaveAndReturn() {
        accuracyRequest.setAccuracyThreshold(null);
        doNothing().when(validator).validateAchievementRequest(any());
        doNothing().when(validator).validateMasterId(anyString());
        when(achievementRepository.findById(ACH_ID)).thenReturn(Optional.of(savedAccuracy));
        when(achievementRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(achievementRepository.save(any())).thenReturn(savedAccuracy);
        when(userAchievementProgressRepository.countByAchievement(any())).thenReturn(0L);

        AchievementResponse result = service.update(ACH_ID, accuracyRequest);

        assertAll("Verify valid accuracy update with null threshold saves and returns response",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> verify(achievementRepository).save(savedAccuracy));
    }

    @Test
    void update_WhenRankingBasedValid_ShouldSaveAndReturn() {
        doNothing().when(validator).validateAchievementRequest(any());
        doNothing().when(validator).validateMasterId(anyString());
        when(achievementRepository.findById(ACH_ID)).thenReturn(Optional.of(savedRanking));
        when(achievementRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(achievementRepository.save(any())).thenReturn(savedRanking);
        when(userAchievementProgressRepository.countByAchievement(any())).thenReturn(0L);

        AchievementResponse result = service.update(ACH_ID, rankingRequest);

        assertAll("Verify valid ranking update saves and returns response",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> verify(achievementRepository).save(savedRanking));
    }

    @Test
    void update_WhenRankingBasedValidWithNullTargetTier_ShouldSaveAndReturn() {
        rankingRequest.setTargetTier(null);
        doNothing().when(validator).validateAchievementRequest(any());
        doNothing().when(validator).validateMasterId(anyString());
        when(achievementRepository.findById(ACH_ID)).thenReturn(Optional.of(savedRanking));
        when(achievementRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(achievementRepository.save(any())).thenReturn(savedRanking);
        when(userAchievementProgressRepository.countByAchievement(any())).thenReturn(0L);

        AchievementResponse result = service.update(ACH_ID, rankingRequest);

        assertAll("Verify valid ranking update with null target tier saves and returns response",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> verify(achievementRepository).save(savedRanking));
    }

    @Test
    void update_WhenRankingBasedValidWithBlankTargetTier_ShouldSaveAndReturn() {
        rankingRequest.setTargetTier("   ");
        doNothing().when(validator).validateAchievementRequest(any());
        doNothing().when(validator).validateMasterId(anyString());
        when(achievementRepository.findById(ACH_ID)).thenReturn(Optional.of(savedRanking));
        when(achievementRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(achievementRepository.save(any())).thenReturn(savedRanking);
        when(userAchievementProgressRepository.countByAchievement(any())).thenReturn(0L);

        AchievementResponse result = service.update(ACH_ID, rankingRequest);

        assertAll("Verify valid ranking update with blank target tier saves and returns response",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> verify(achievementRepository).save(savedRanking));
    }

    // ─── delete ──────────────────────────────────────────────────────────────

    @Test
    void delete_WhenNotFound_ShouldThrow() {
        doNothing().when(validator).validateMasterId(anyString());
        when(achievementRepository.findById(ACH_ID)).thenReturn(Optional.empty());

        assertAll("Verify exception when deleting non-existent achievement",
                () -> {
                    GamificationException ex = assertThrows(GamificationException.class,
                            () -> service.delete(ACH_ID), MSG_THROW);
                    assertEquals("NOT_FOUND", ex.getErrorCode(), MSG_CODE_404);
                });
    }

    @Test
    void delete_WhenValid_ShouldDeleteFromRepository() {
        doNothing().when(validator).validateMasterId(anyString());
        when(achievementRepository.findById(ACH_ID)).thenReturn(Optional.of(savedCount));

        assertAll("Verify valid delete removes from repository",
                () -> assertDoesNotThrow(() -> service.delete(ACH_ID), "Should delete without throwing"),
            () -> verify(achievementRepository).delete(savedCount));
    }

    // ─── findAll / getAll / getByIds ──────────────────────────────────────────

    @Test
    void findAll_ShouldReturnMappedResponses() {
        when(achievementRepository.findAll()).thenReturn(List.of(savedCount, savedAccuracy));
        when(userAchievementProgressRepository.countByAchievement(any())).thenReturn(0L);

        List<AchievementResponse> result = service.findAll();

        assertAll("Verify findAll returns mapped responses",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertEquals(2, result.size(), MSG_LIST_SIZE));
    }

    @Test
    void getAllAchievements_ShouldReturnEntities() {
        when(achievementRepository.findAll()).thenReturn(List.of(savedCount));

        List<Achievement> result = service.getAllAchievements();

        assertAll("Verify getAllAchievements returns entity list",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertEquals(1, result.size(), MSG_LIST_SIZE));
    }

    @Test
    void getAchievementsByIds_WhenEmpty_ShouldReturnEmptyList() {
        List<Achievement> result = service.getAchievementsByIds(List.of());

        assertAll("Verify empty ids returns empty list",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertEquals(0, result.size(), MSG_LIST_SIZE));
    }

    @Test
    void getAchievementsByIds_WhenNull_ShouldReturnEmptyList() {
        List<Achievement> result = service.getAchievementsByIds(null);

        assertAll("Verify null ids returns empty list",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertEquals(0, result.size(), MSG_LIST_SIZE));
    }

    @Test
    void getAchievementsByIds_WhenValid_ShouldReturnEntities() {
        when(achievementRepository.findAllById(any())).thenReturn(List.of(savedCount, savedRanking));

        List<Achievement> result = service.getAchievementsByIds(List.of(ACH_ID, "ach-002"));

        assertAll("Verify getAchievementsByIds returns matching entities",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertEquals(2, result.size(), MSG_LIST_SIZE));
    }
}
