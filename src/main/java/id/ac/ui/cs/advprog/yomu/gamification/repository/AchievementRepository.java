package id.ac.ui.cs.advprog.yomu.gamification.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;

public interface AchievementRepository extends JpaRepository<Achievement, String> {
    Optional<Achievement> findByNameIgnoreCase(String name);

    long countByActiveTrue();
}
