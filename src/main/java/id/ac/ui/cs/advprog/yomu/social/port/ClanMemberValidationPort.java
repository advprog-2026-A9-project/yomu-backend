package id.ac.ui.cs.advprog.yomu.social.port;

public interface ClanMemberValidationPort {
    boolean existsByClanIdAndUsername(String clanId, String username);
    boolean existsByUsername(String username);
    long countByClanId(String clanId);
}
