package id.ac.ui.cs.advprog.yomu.social.validation;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Concrete implementation of ClanValidator.
 */
@Component
public class ClanValidatorImpl implements ClanValidator {

    private static final int MAX_CLAN_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;

    private void validateId(final String id, final String errorMessage) {
        if (id == null || id.isBlank()) {
            throw new IllegalStateException(errorMessage);
        }
    }

    @Override
    public void requireClanId(final String clanId) {
        validateId(clanId, SocialConstants.CLAN_ID_NULL_MESSAGE);
    }

    @Override
    public void requireUserId(final String userId) {
        validateId(userId, SocialConstants.USER_ID_NULL_MESSAGE);
    }

    @Override
    public void requireValidClanName(final String clanName) {
        if (clanName == null || clanName.isBlank()) {
            throw new IllegalArgumentException("Clan name cannot be empty");
        }
        if (clanName.length() > MAX_CLAN_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "Clan name exceeds maximum length of " + MAX_CLAN_NAME_LENGTH);
        }
    }

    @Override
    public void requireValidClanDescription(final String description) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException(
                    "Clan description exceeds maximum length of " + MAX_DESCRIPTION_LENGTH);
        }
    }

    @Override
    public void requireClanNameAvailable(final boolean nameExists) {
        if (nameExists) {
            throw new IllegalArgumentException(SocialConstants.CLAN_NAME_ALREADY_USED_MESSAGE);
        }
    }

    @Override
    public void requireLeaderPrivilege(final Clan clan, final String userId, final String errorMessage) {
        if (!Objects.equals(clan.getLeaderUserId(), userId)) {
            throw new IllegalStateException(errorMessage);
        }
    }

    @Override
    public void requireNotAlreadyMember(final boolean isAlreadyMember) {
        if (isAlreadyMember) {
            throw new IllegalStateException(SocialConstants.ALREADY_MEMBER_MESSAGE);
        }
    }

    @Override
    public void requireNotMemberOfOtherClan(final boolean alreadyInOtherClan) {
        if (alreadyInOtherClan) {
            throw new IllegalStateException(SocialConstants.ALREADY_IN_OTHER_CLAN_MESSAGE);
        }
    }

    @Override
    public void requireClanNotFull(final long currentMemberCount) {
        if (currentMemberCount >= SocialConstants.MAX_CLAN_SIZE) {
            throw new IllegalStateException(SocialConstants.CLAN_FULL_MESSAGE);
        }
    }

    @Override
    public String resolveReplacementLeader(final List<ClanMember> allMembers, final String currentLeaderId) {
        return allMembers.stream()
                .map(ClanMember::getUserId)
                .filter(userId -> !userId.equals(currentLeaderId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(SocialConstants.FAILED_FIND_REPLACEMENT_LEADER_MESSAGE));
    }
}
