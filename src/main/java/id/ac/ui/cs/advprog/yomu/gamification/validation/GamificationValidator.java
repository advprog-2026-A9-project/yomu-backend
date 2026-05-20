package id.ac.ui.cs.advprog.yomu.gamification.validation;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementRequest;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionRequest;

/**
 * Abstraction for gamification input validation
 * Dependency Inversion: services depend on this interface, not on concrete validation logic
 */
public interface GamificationValidator {

    /**
     * Validate achievement request
     */
    void validateAchievementRequest(AchievementRequest request);

    /**
     * Validate daily mission request
     */
    void validateDailyMissionRequest(DailyMissionRequest request);

    /**
     * Validate username exists and is not empty
     */
    void validateUsername(String username);

    /**
     * Validate master data ID exists and is not empty
     */
    void validateMasterId(String masterId);
}
