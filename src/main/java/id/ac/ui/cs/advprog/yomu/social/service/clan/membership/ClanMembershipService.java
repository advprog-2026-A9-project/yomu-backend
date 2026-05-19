package id.ac.ui.cs.advprog.yomu.social.service.clan.membership;

import java.util.List;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;

public interface ClanMembershipService {
    void joinClan(String clanId, String username, String role);
    void leaveClan(String clanId, String username);
    void kickMember(String clanId, String leaderUsername, String memberUsername);
    List<ClanMember> getMembersByClanId(String clanId);
}
