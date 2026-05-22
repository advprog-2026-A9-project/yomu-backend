package id.ac.ui.cs.advprog.yomu.profile.repository;

import id.ac.ui.cs.advprog.yomu.profile.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, String> {
    Optional<Profile> findByUsername(String username);
    List<Profile> findByClanId(String clanId);
}
