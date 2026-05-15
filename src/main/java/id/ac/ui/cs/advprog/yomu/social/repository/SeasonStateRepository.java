package id.ac.ui.cs.advprog.yomu.social.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import id.ac.ui.cs.advprog.yomu.social.model.SeasonState;

public interface SeasonStateRepository extends JpaRepository<SeasonState, Long> {
    Optional<SeasonState> findTopByOrderByIdDesc();
}