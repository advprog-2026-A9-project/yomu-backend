package id.ac.ui.cs.advprog.yomu.social.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import id.ac.ui.cs.advprog.yomu.social.model.Clan;

@Repository
public interface ClanRepository extends JpaRepository<Clan, String> {
    Optional<Clan> findByName(String name);
    boolean existsByName(String name);
}