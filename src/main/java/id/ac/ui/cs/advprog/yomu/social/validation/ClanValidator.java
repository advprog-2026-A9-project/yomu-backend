package id.ac.ui.cs.advprog.yomu.social.validation;

import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;

import java.util.List;

/**
 * Interface abstraction for clan validation logic.
 * This allows high-level services to depend on abstractions rather than
 * concrete implementations.
 */
public interface ClanValidator {

    void requireClanId(String clanId);

    void requireUserId(String userId);

    void requireValidClanName(String clanName);

    void requireValidClanDescription(String description);

    void requireClanNameAvailable(boolean nameExists);

    void requireLeaderPrivilege(Clan clan, String userId, String errorMessage);

    void requireNotAlreadyMember(boolean isAlreadyMember);

    void requireNotMemberOfOtherClan(boolean alreadyInOtherClan);

    void requireClanNotFull(long currentMemberCount);

    String resolveReplacementLeader(List<ClanMember> allMembers, String currentLeaderId);
}
