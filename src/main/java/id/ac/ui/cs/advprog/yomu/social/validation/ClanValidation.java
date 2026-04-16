package id.ac.ui.cs.advprog.yomu.social.validation;

import java.util.List;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;

public final class ClanValidation {

    private ClanValidation() {
    }

    public static void requireClanId(final String clanId) {
        if (clanId == null) {
            throw new IllegalStateException(SocialConstants.CLAN_ID_NULL_MESSAGE);
        }
    }

    public static void requireUserId(final String userId) {
        if (userId == null) {
            throw new IllegalStateException(SocialConstants.USER_ID_NULL_MESSAGE);
        }
    }

    public static void requireClanNameAvailable(final boolean clanExists) {
        if (clanExists) {
            throw new IllegalArgumentException(SocialConstants.CLAN_NAME_ALREADY_USED_MESSAGE);
        }
    }

    public static void requireLeaderCanEdit(final Clan clan, final String userId) {
        if (!clan.getLeaderUserId().equals(userId)) {
            throw new IllegalStateException(SocialConstants.ONLY_LEADER_CAN_EDIT_MESSAGE);
        }
    }

    public static void requireClanExists(final boolean clanExists) {
        if (!clanExists) {
            throw new IllegalArgumentException(SocialConstants.CLAN_NOT_FOUND_MESSAGE);
        }
    }

    public static void requireMemberNotAlreadyInClan(final boolean alreadyMember) {
        if (alreadyMember) {
            throw new IllegalStateException(SocialConstants.ALREADY_MEMBER_MESSAGE);
        }
    }

    public static void requireMemberNotInOtherClan(final boolean alreadyInOtherClan) {
        if (alreadyInOtherClan) {
            throw new IllegalStateException(SocialConstants.ALREADY_IN_OTHER_CLAN_MESSAGE);
        }
    }

    public static void requireLeaderCanDelete(final Clan clan, final String leaderId) {
        if (!clan.getLeaderUserId().equals(leaderId)) {
            throw new IllegalStateException(SocialConstants.ONLY_LEADER_CAN_DELETE_MESSAGE);
        }
    }

    public static String resolveReplacementLeader(final List<ClanMember> allMembers, final String leaderId) {
        return allMembers.stream()
                .map(ClanMember::getUserId)
                .filter(id -> !id.equals(leaderId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(SocialConstants.FAILED_FIND_REPLACEMENT_LEADER_MESSAGE));
    }
}