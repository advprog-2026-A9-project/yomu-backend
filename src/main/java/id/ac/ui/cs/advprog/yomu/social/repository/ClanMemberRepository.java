package id.ac.ui.cs.advprog.yomu.social.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import jakarta.transaction.Transactional;

@Repository
public interface ClanMemberRepository extends JpaRepository<ClanMember, Long> {
    
    List<ClanMember> findByClanId(String clanId);
    
    Optional<ClanMember> findByClanIdAndUserId(String clanId, String userId);
    
    Optional<ClanMember> findByUserId(String userId);

    @Transactional
    void deleteByClanIdAndUserId(String clanId, String userId);
}