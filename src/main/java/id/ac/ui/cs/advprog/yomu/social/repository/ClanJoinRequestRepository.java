package id.ac.ui.cs.advprog.yomu.social.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import id.ac.ui.cs.advprog.yomu.social.model.ClanJoinRequest;
import id.ac.ui.cs.advprog.yomu.social.model.ClanJoinRequestStatus;

@Repository
public interface ClanJoinRequestRepository extends JpaRepository<ClanJoinRequest, Long> {
    Page<ClanJoinRequest> findByClanIdAndStatus(String clanId, ClanJoinRequestStatus status, Pageable pageable);
    List<ClanJoinRequest> findByClanIdAndStatus(String clanId, ClanJoinRequestStatus status);
    Optional<ClanJoinRequest> findByClanIdAndUsernameAndStatus(String clanId, String username, ClanJoinRequestStatus status);
    List<ClanJoinRequest> findByUsernameAndStatus(String username, ClanJoinRequestStatus status);

    @Modifying
    @Query("UPDATE ClanJoinRequest c SET c.status = :newStatus WHERE c.clanId = :clanId AND c.status = :oldStatus")
    void updateStatusByClanIdAndStatus(@Param("clanId") String clanId, @Param("oldStatus") ClanJoinRequestStatus oldStatus,
            @Param("newStatus") ClanJoinRequestStatus newStatus);
}
