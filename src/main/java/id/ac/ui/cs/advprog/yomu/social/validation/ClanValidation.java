package id.ac.ui.cs.advprog.yomu.social.validation;

import java.util.List;
import java.util.Objects;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;

/**
 * Utility class for clan-related business rule validations.
 * This file acts as the 'Gatekeeper' to ensure data integrity before
 * the Service layer performs any database operations.
 */
public final class ClanValidation {

    private ClanValidation() {
        // Prevent instantiation
    }


    private static void validateId(final String id, final String errorMessage) {
        if (id == null || id.isBlank()) {
            throw new IllegalStateException(errorMessage);
        }
    }

    public static void requireClanId(final String clanId) {
        validateId(clanId, SocialConstants.CLAN_ID_NULL_MESSAGE);
    }

    public static void requireUserId(final String userId) {
        validateId(userId, SocialConstants.USER_ID_NULL_MESSAGE);
    }

    public static void requireClanNameAvailable(final boolean nameExists) {
        if (nameExists) {
            throw new IllegalArgumentException(SocialConstants.CLAN_NAME_ALREADY_USED_MESSAGE);
        }
    }

    public static void requireLeaderPrivilege(final Clan clan, final String userId, final String errorMessage) {
        if (!Objects.equals(clan.getLeaderUserId(), userId)) {
            throw new IllegalStateException(errorMessage);
        }
    }


    public static void requireNotAlreadyMember(final boolean isAlreadyMember) {
        if (isAlreadyMember) {
            throw new IllegalStateException(SocialConstants.ALREADY_MEMBER_MESSAGE);
        }
    }

    public static void requireNotMemberOfOtherClan(final boolean alreadyInOtherClan) {
        if (alreadyInOtherClan) {
            throw new IllegalStateException(SocialConstants.ALREADY_IN_OTHER_CLAN_MESSAGE);
        }
    }

    public static String resolveReplacementLeader(final List<ClanMember> allMembers, final String currentLeaderId) {
        return allMembers.stream()
                .map(ClanMember::getUserId)
                .filter(userId -> !userId.equals(currentLeaderId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(SocialConstants.FAILED_FIND_REPLACEMENT_LEADER_MESSAGE));
    }
}