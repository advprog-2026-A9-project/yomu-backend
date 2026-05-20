package id.ac.ui.cs.advprog.yomu.gamification.validation;

import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementRequest;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionRequest;
import id.ac.ui.cs.advprog.yomu.gamification.exception.GamificationException;
import id.ac.ui.cs.advprog.yomu.gamification.model.AchievementMilestoneType;
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMissionType;

/**
 * Implementation of gamification validator
 * Dependency Inversion: concrete validation logic is decoupled from services
 */
@Component
public class GamificationValidatorImpl implements GamificationValidator {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_MILESTONE_LENGTH = 255;
    private static final String CHARACTERS_SUFFIX = " characters";

    @Override
    public void validateAchievementRequest(AchievementRequest request) {
        if (request == null) {
            throw new GamificationException("Achievement request cannot be null", "INVALID_REQUEST");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new GamificationException("Achievement name cannot be empty", "INVALID_NAME");
        }

        if (request.getName().trim().length() > MAX_NAME_LENGTH) {
            throw new GamificationException(
                "Achievement name cannot exceed " + MAX_NAME_LENGTH + CHARACTERS_SUFFIX,
                "NAME_TOO_LONG"
            );
        }

        if (request.getMilestone() == null || request.getMilestone().trim().isEmpty()) {
            throw new GamificationException("Achievement milestone cannot be empty", "INVALID_MILESTONE");
        }

        if (request.getMilestone().trim().length() > MAX_MILESTONE_LENGTH) {
            throw new GamificationException(
                "Achievement milestone cannot exceed " + MAX_MILESTONE_LENGTH + CHARACTERS_SUFFIX,
                "MILESTONE_TOO_LONG"
            );
        }

        if (request.getMilestoneType() == null || request.getMilestoneType().trim().isEmpty()) {
            throw new GamificationException("Achievement milestone type cannot be empty", "INVALID_MILESTONE_TYPE");
        }

        AchievementMilestoneType milestoneType = AchievementMilestoneType.from(request.getMilestoneType());
        if (milestoneType == null) {
            throw new GamificationException("Achievement milestone type is invalid", "INVALID_MILESTONE_TYPE");
        }

        if (request.getMilestoneThreshold() <= 0) {
            throw new GamificationException("Achievement threshold must be positive", "INVALID_MILESTONE_THRESHOLD");
        }

        if (milestoneType == AchievementMilestoneType.RANKING_ACHIEVED) {
            if (request.getTargetTier() == null || request.getTargetTier().trim().isEmpty()) {
                throw new GamificationException("Target tier is required for ranking achieved achievements", "INVALID_TARGET_TIER");
            }
            String targetTierTrimmed = request.getTargetTier().trim();
            boolean isValidTier = false;
            for (String allowedTier : new String[]{"BRONZE", "SILVER", "GOLD", "DIAMOND", "MYTHIC", "GODLIKE"}) {
                if (allowedTier.equalsIgnoreCase(targetTierTrimmed)) {
                    isValidTier = true;
                    break;
                }
            }
            if (!isValidTier) {
                throw new GamificationException("Invalid target tier", "INVALID_TARGET_TIER");
            }
        }
    }

    @Override
    public void validateDailyMissionRequest(DailyMissionRequest request) {
        if (request == null) {
            throw new GamificationException("Daily mission request cannot be null", "INVALID_REQUEST");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new GamificationException("Daily mission name cannot be empty", "INVALID_NAME");
        }

        if (request.getName().trim().length() > MAX_NAME_LENGTH) {
            throw new GamificationException(
                "Daily mission name cannot exceed " + MAX_NAME_LENGTH + CHARACTERS_SUFFIX,
                "NAME_TOO_LONG"
            );
        }

        if (request.getMilestone() == null || request.getMilestone().trim().isEmpty()) {
            throw new GamificationException("Daily mission milestone cannot be empty", "INVALID_MILESTONE");
        }

        if (request.getMilestone().trim().length() > MAX_MILESTONE_LENGTH) {
            throw new GamificationException(
                "Daily mission milestone cannot exceed " + MAX_MILESTONE_LENGTH + CHARACTERS_SUFFIX,
                "MILESTONE_TOO_LONG"
            );
        }

        if (request.getMissionType() == null || request.getMissionType().trim().isEmpty()) {
            throw new GamificationException("Daily mission type cannot be empty", "INVALID_MISSION_TYPE");
        }

        DailyMissionType missionType = DailyMissionType.from(request.getMissionType());
        if (missionType == null) {
            throw new GamificationException("Daily mission type is invalid", "INVALID_MISSION_TYPE");
        }

        if (missionType == DailyMissionType.ACHIEVE_ACCURACY) {
            if (request.getAccuracyThreshold() == null || request.getAccuracyThreshold() <= 0 || request.getAccuracyThreshold() > 100) {
                throw new GamificationException("Accuracy threshold must be between 1 and 100", "INVALID_ACCURACY_THRESHOLD");
            }
            if (request.getRequiredCount() == null || request.getRequiredCount() <= 0) {
                throw new GamificationException("Required count must be positive", "INVALID_REQUIRED_COUNT");
            }
        } else {
            if (request.getTargetCount() == null || request.getTargetCount() <= 0) {
                throw new GamificationException("Daily mission target count must be positive", "INVALID_TARGET_COUNT");
            }
        }

        if (request.getRewardScore() == null || request.getRewardScore() < 1) {
            throw new GamificationException("Reward score must be a strictly positive integer", "INVALID_REWARD_SCORE");
        }
    }

    @Override
    public void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new GamificationException("Username cannot be empty", "INVALID_USERNAME");
        }
    }

    @Override
    public void validateMasterId(String masterId) {
        if (masterId == null || masterId.trim().isEmpty()) {
            throw new GamificationException("Master data ID cannot be empty", "INVALID_MASTER_ID");
        }
    }
}
