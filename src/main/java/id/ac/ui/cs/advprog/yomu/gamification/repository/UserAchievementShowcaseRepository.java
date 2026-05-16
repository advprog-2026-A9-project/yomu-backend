package id.ac.ui.cs.advprog.yomu.gamification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementShowcase;

@Repository
public interface UserAchievementShowcaseRepository extends JpaRepository<UserAchievementShowcase, String> {
}
