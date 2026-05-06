package id.ac.ui.cs.advprog.yomu.social.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import id.ac.ui.cs.advprog.yomu.social.model.ClanQuizStats;

@Repository
public interface ClanQuizStatsRepository extends JpaRepository<ClanQuizStats, String> {
}
