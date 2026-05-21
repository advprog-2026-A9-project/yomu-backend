package id.ac.ui.cs.advprog.yomu.gamification.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserDailyMissionProgress;

public interface UserDailyMissionProgressRepository extends JpaRepository<UserDailyMissionProgress, String> {
    List<UserDailyMissionProgress> findByUsernameAndProgressDate(String username, LocalDate progressDate);

    Optional<UserDailyMissionProgress> findByUsernameAndDailyMissionAndProgressDate(
        String username,
        DailyMission dailyMission,
        LocalDate progressDate
    );
}
