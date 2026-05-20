package id.ac.ui.cs.advprog.yomu.gamification.service.achievement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.ProgressUpdateRequest;
import id.ac.ui.cs.advprog.yomu.gamification.exception.GamificationException;
import id.ac.ui.cs.advprog.yomu.gamification.mapper.GamificationMapper;
import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;
import id.ac.ui.cs.advprog.yomu.gamification.repository.AchievementRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserAchievementProgressRepository;
import id.ac.ui.cs.advprog.yomu.gamification.validation.GamificationValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class AchievementProgressServiceImpl implements AchievementProgressService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementProgressRepository userAchievementProgressRepository;
    private final GamificationValidator validator;
    private final GamificationMapper mapper;

    @Override
    @Transactional
    public UserAchievementProgress getOrCreateAchievementProgress(String username, Achievement achievement) {
        return userAchievementProgressRepository
                .findByUsernameAndAchievement(username, achievement)
                .orElseGet(() -> {
                    UserAchievementProgress p = new UserAchievementProgress();
                    p.setUsername(username);
                    p.setAchievement(achievement);
                    p.setProgressValue(0);
                    p.setUnlocked(false);
                    return p;
                });
    }

    @Override
    @Transactional
    public AchievementProgressResponse upsertAchievementProgress(ProgressUpdateRequest request) {
        validator.validateMasterId(request.getMasterId());
        validator.validateUsername(request.getUsername());

        String safeMasterId = Objects.requireNonNull(request.getMasterId());
        String safeUsername = Objects.requireNonNull(request.getUsername());

        Achievement achievement = achievementRepository.findById(safeMasterId)
                .orElseThrow(() -> new GamificationException(
                        "Achievement not found",
                        "NOT_FOUND"));

        UserAchievementProgress progress = getOrCreateAchievementProgress(safeUsername, achievement);

        progress.setProgressValue(request.getProgressValue());

        if (!progress.isUnlocked() && request.getProgressValue() >= achievement.getMilestoneThreshold()) {
            progress.setUnlocked(true);
            progress.setUnlockedAt(LocalDateTime.now());
        }

        UserAchievementProgress saved = userAchievementProgressRepository.save(progress);
        return mapper.toAchievementProgressResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementProgressResponse> getAchievementProgressByUsername(String username) {
        validator.validateUsername(username);

        List<Achievement> activeAchievements = achievementRepository.findByActiveTrue();

        return activeAchievements.stream().map(achievement -> {
            UserAchievementProgress progress = getOrCreateAchievementProgress(username, achievement);
            return mapper.toAchievementProgressResponse(progress);
        }).toList();
    }

    @Override
    @Transactional
    public void saveProgress(UserAchievementProgress progress) {
        userAchievementProgressRepository.save(progress);
    }
}
