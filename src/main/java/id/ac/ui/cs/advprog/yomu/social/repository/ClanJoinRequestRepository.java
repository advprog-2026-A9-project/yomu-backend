package id.ac.ui.cs.advprog.yomu.social.repository;

import id.ac.ui.cs.advprog.yomu.social.model.ClanJoinRequest;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClanJoinRequestRepository extends JpaRepository<ClanJoinRequest, Long> {
    Page<ClanJoinRequest> findByClanIdAndStatus(String clanId, String status, Pageable pageable);
    List<ClanJoinRequest> findByClanIdAndStatus(String clanId, String status);
    Optional<ClanJoinRequest> findByClanIdAndUsernameAndStatus(String clanId, String username, String status);
    List<ClanJoinRequest> findByUsernameAndStatus(String username, String status);

    @Modifying
    @Query("UPDATE ClanJoinRequest c SET c.status = :newStatus WHERE c.clanId = :clanId AND c.status = :oldStatus")
    void updateStatusByClanIdAndStatus(@Param("clanId") String clanId, @Param("oldStatus") String oldStatus, @Param("newStatus") String newStatus);
}
