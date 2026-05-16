package id.ac.ui.cs.advprog.yomu.gamification.validation;

import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementRequest;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionRequest;
import id.ac.ui.cs.advprog.yomu.gamification.exception.GamificationException;

/**
 * Implementation of gamification validator
 * Dependency Inversion: concrete validation logic is decoupled from services
 */
@Component
public class GamificationValidatorImpl implements GamificationValidator {

    private static final String[] ACHIEVEMENT_TYPES = {
        "readings_completed",
        "quizzes_passed",
        "accuracy_above",
        "clan_promoted"
    };

    private static final String[] DAILY_MISSION_TYPES = {
        "read_n_articles",
        "complete_n_quizzes",
        "achieve_accuracy"
    };

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

        if (!isAllowedType(request.getMilestoneType(), ACHIEVEMENT_TYPES)) {
            throw new GamificationException("Achievement milestone type is invalid", "INVALID_MILESTONE_TYPE");
        }

        if (request.getMilestoneThreshold() <= 0) {
            throw new GamificationException("Achievement threshold must be positive", "INVALID_MILESTONE_THRESHOLD");
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

        if (!isAllowedType(request.getMissionType(), DAILY_MISSION_TYPES)) {
            throw new GamificationException("Daily mission type is invalid", "INVALID_MISSION_TYPE");
        }

        if (request.getTargetCount() <= 0) {
            throw new GamificationException("Daily mission target count must be positive", "INVALID_TARGET_COUNT");
        }

        if (request.getRewardDescription() == null || request.getRewardDescription().trim().isEmpty()) {
            throw new GamificationException("Reward description cannot be empty", "INVALID_REWARD_DESCRIPTION");
        }
    }

    @Override
    public void validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new GamificationException("User ID cannot be empty", "INVALID_USER_ID");
        }
    }

    @Override
    public void validateMasterId(String masterId) {
        if (masterId == null || masterId.trim().isEmpty()) {
            throw new GamificationException("Master data ID cannot be empty", "INVALID_MASTER_ID");
        }
    }

    private boolean isAllowedType(String value, String... allowedTypes) {
        for (String allowedType : allowedTypes) {
            if (allowedType.equalsIgnoreCase(value.trim())) {
                return true;
            }
        }
        return false;
    }
}
