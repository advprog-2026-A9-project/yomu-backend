package id.ac.ui.cs.advprog.yomu.gamification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;

public interface UserAchievementProgressRepository extends JpaRepository<UserAchievementProgress, String> {
    List<UserAchievementProgress> findByUserId(String userId);

    Optional<UserAchievementProgress> findByUserIdAndAchievement(String userId, Achievement achievement);

    long countByAchievement(Achievement achievement);
}
