package id.ac.ui.cs.advprog.yomu.gamification.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementRequest;
import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementResponse;
import id.ac.ui.cs.advprog.yomu.gamification.exception.GamificationException;
import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.repository.AchievementRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserAchievementProgressRepository;
import id.ac.ui.cs.advprog.yomu.gamification.validation.GamificationValidator;
import lombok.RequiredArgsConstructor;

/**
 * Achievement service implementation
 * Dependency Inversion: depends on GamificationValidator and GamificationMapper interfaces
 */
@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {

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
                "DUPLICATE_NAME"
            );
        });

        Achievement achievement = new Achievement();
        achievement.setName(request.getName().trim());
        achievement.setMilestone(request.getMilestone().trim());
        achievement.setMilestoneType(request.getMilestoneType().trim());
        achievement.setMilestoneThreshold(request.getMilestoneThreshold());

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
                    "DUPLICATE_NAME"
                );
            });

        achievement.setName(request.getName().trim());
        achievement.setMilestone(request.getMilestone().trim());
        achievement.setMilestoneType(request.getMilestoneType().trim());
        achievement.setMilestoneThreshold(request.getMilestoneThreshold());

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

    private AchievementResponse toResponse(Achievement achievement) {
        return new AchievementResponse(
            achievement.getId(),
            achievement.getName(),
            achievement.getMilestone(),
            achievement.getMilestoneType(),
            achievement.getMilestoneThreshold(),
            userAchievementProgressRepository.countByAchievement(achievement),
            achievement.isActive()
        );
    }
}
