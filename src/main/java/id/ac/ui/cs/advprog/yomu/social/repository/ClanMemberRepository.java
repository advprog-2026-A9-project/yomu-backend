package id.ac.ui.cs.advprog.yomu.social.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.port.ClanMemberValidationPort;
import jakarta.transaction.Transactional;

@Repository
public interface ClanMemberRepository extends JpaRepository<ClanMember, Long>, ClanMemberValidationPort {
    
    List<ClanMember> findByClanId(String clanId);
    
    Optional<ClanMember> findByClanIdAndUsername(String clanId, String username);
    
    Optional<ClanMember> findByUsername(String username);

    List<ClanMember> getClanMembersByClanId(String clanId);
    
    @Override
    long countByClanId(String clanId);

    @Transactional
    void deleteByClanIdAndUsername(String clanId, String username);

    @Transactional
    void deleteByClanId(String clanId);
}