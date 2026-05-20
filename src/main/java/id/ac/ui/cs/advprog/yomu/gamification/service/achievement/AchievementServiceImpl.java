package id.ac.ui.cs.advprog.yomu.gamification.service.achievement;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementRequest;
import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementResponse;
import id.ac.ui.cs.advprog.yomu.gamification.exception.GamificationException;
import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.AccuracyBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.RankingBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.repository.AchievementRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserAchievementProgressRepository;
import id.ac.ui.cs.advprog.yomu.gamification.validation.GamificationValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {

    private static final String MILESTONE_TYPE_ACCURACY = "accuracy_above";
    private static final String MILESTONE_TYPE_RANKING = "ranking_achieved";

    private final AchievementRepository achievementRepository;
    private final UserAchievementProgressRepository userAchievementProgressRepository;
    private final GamificationValidator validator;

    @Override
    @Transactional
    public AchievementResponse create(AchievementRequest request) {
        validator.validateAchievementRequest(request);

        achievementRepository.findByNameIgnoreCase(request.getName()).ifPresent(existing -> {
            throw new GamificationException(
                    "Achievement with this name already exists",
                    "DUPLICATE_NAME");
        });

        Achievement achievement;
        if (MILESTONE_TYPE_ACCURACY.equalsIgnoreCase(request.getMilestoneType().trim())) {
            AccuracyBasedAchievement accAch = new AccuracyBasedAchievement();
            accAch.setAccuracyThreshold(request.getAccuracyThreshold() != null ? request.getAccuracyThreshold() : 0);
            achievement = accAch;
        } else if (MILESTONE_TYPE_RANKING.equalsIgnoreCase(request.getMilestoneType().trim())) {
            RankingBasedAchievement rankAch = new RankingBasedAchievement();
            rankAch.setTargetTier(request.getTargetTier() != null && !request.getTargetTier().isBlank()
                    ? request.getTargetTier().trim().toUpperCase(Locale.ROOT)
                    : null);
            achievement = rankAch;
        } else {
            achievement = new CountBasedAchievement();
        }
        achievement.setName(request.getName().trim());
        achievement.setMilestone(request.getMilestone().trim());
        achievement.setMilestoneType(request.getMilestoneType().trim());
        achievement.setMilestoneThreshold(request.getMilestoneThreshold());
        achievement.setTier(request.getTier() != null && !request.getTier().isBlank()
                ? request.getTier().trim().toUpperCase(Locale.ROOT)
                : null);

        Achievement saved = achievementRepository.save(achievement);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AchievementResponse update(String achievementId, AchievementRequest request) {
        validator.validateAchievementRequest(request);
        validator.validateMasterId(achievementId);

        String safeAchievementId = Objects.requireNonNull(achievementId);
        Achievement achievement = achievementRepository.findById(safeAchievementId)
                .orElseThrow(() -> new GamificationException("Achievement not found", "NOT_FOUND"));

        achievementRepository.findByNameIgnoreCase(request.getName())
                .filter(existing -> !existing.getId().equals(safeAchievementId))
                .ifPresent(existing -> {
                    throw new GamificationException(
                            "Achievement with this name already exists",
                            "DUPLICATE_NAME");
                });

        boolean isRequestAccuracy = MILESTONE_TYPE_ACCURACY.equalsIgnoreCase(request.getMilestoneType().trim());
        boolean isExistingAccuracy = achievement instanceof AccuracyBasedAchievement;
        boolean isRequestRanking = MILESTONE_TYPE_RANKING.equalsIgnoreCase(request.getMilestoneType().trim());
        boolean isExistingRanking = achievement instanceof RankingBasedAchievement;

        if (isRequestAccuracy != isExistingAccuracy || isRequestRanking != isExistingRanking) {
            throw new GamificationException(
                    "Cannot change achievement category",
                    "INVALID_TYPE_CHANGE");
        }

        achievement.setName(request.getName().trim());
        achievement.setMilestone(request.getMilestone().trim());
        achievement.setMilestoneType(request.getMilestoneType().trim());
        achievement.setMilestoneThreshold(request.getMilestoneThreshold());
        if (achievement instanceof AccuracyBasedAchievement accuracyAchievement) {
            accuracyAchievement
                    .setAccuracyThreshold(request.getAccuracyThreshold() != null ? request.getAccuracyThreshold() : 0);
        }
        if (achievement instanceof RankingBasedAchievement rankingAchievement) {
            rankingAchievement.setTargetTier(request.getTargetTier() != null && !request.getTargetTier().isBlank()
                    ? request.getTargetTier().trim().toUpperCase(Locale.ROOT)
                    : null);
        }
        achievement.setTier(request.getTier() != null && !request.getTier().isBlank()
                ? request.getTier().trim().toUpperCase(Locale.ROOT)
                : null);

        return toResponse(achievementRepository.save(achievement));
    }

    @Override
    @Transactional
    public void delete(String achievementId) {
        validator.validateMasterId(achievementId);

        String safeAchievementId = Objects.requireNonNull(achievementId);
        Achievement achievement = achievementRepository.findById(safeAchievementId)
                .orElseThrow(() -> new GamificationException("Achievement not found", "NOT_FOUND"));

        achievementRepository.delete(Objects.requireNonNull(achievement));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementResponse> findAll() {
        return achievementRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Achievement> getAllAchievements() {
        return achievementRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Achievement> getAchievementsByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return achievementRepository.findAllById(ids);
    }

    private AchievementResponse toResponse(Achievement achievement) {
        int accuracyThreshold = 0;
        String targetTier = null;
        if (achievement instanceof AccuracyBasedAchievement accuracyAchievement) {
            accuracyThreshold = accuracyAchievement.getAccuracyThreshold();
        }
        if (achievement instanceof RankingBasedAchievement rankingAchievement) {
            targetTier = rankingAchievement.getTargetTier();
        }
        return new AchievementResponse(
                achievement.getId(),
                achievement.getName(),
                achievement.getMilestone(),
                achievement.getMilestoneType(),
                achievement.getMilestoneThreshold(),
                accuracyThreshold,
                achievement.getTier(),
                targetTier,
                userAchievementProgressRepository.countByAchievement(achievement),
                achievement.isActive());
    }
}
