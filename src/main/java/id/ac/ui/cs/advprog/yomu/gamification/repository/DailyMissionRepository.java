package id.ac.ui.cs.advprog.yomu.gamification.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;

public interface DailyMissionRepository extends JpaRepository<DailyMission, String> {
    Optional<DailyMission> findByNameIgnoreCase(String name);

    long countByActiveTrue();

    List<DailyMission> findByActiveTrueAndActiveFromLessThanEqualAndActiveUntilGreaterThanEqual(LocalDate from, LocalDate until);
}
