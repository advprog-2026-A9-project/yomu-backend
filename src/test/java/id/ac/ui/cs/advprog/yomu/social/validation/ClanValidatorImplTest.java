package id.ac.ui.cs.advprog.yomu.social.validation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;

@SuppressWarnings("PMD")
class ClanValidatorImplTest {

    private static final String CLAN_ID = "clan-123";
    private static final String USERNAME = "user-123";
    private static final String OTHER_USER = "other-user";
    private static final String ERROR_MSG = "Some error message";

    // Assertion Messages
    private static final String MSG_EXCEPTION_MSG = "Exception message should match";
    private static final String MSG_REPLACEMENT_LEADER = "Replacement leader should match";

    private ClanValidatorImpl validator;

    @BeforeEach
    void setUp() {
        validator = new ClanValidatorImpl();
    }

    @Test
    void requireClanId_Valid_DoesNotThrow() {
        validator.requireClanId(CLAN_ID);
    }

    @Test
    void requireClanId_NullOrBlank_ThrowsIllegalStateException() {
        IllegalStateException ex1 = assertThrows(IllegalStateException.class, () ->
            validator.requireClanId(null),
            "Should throw IllegalStateException on null clanId"
        );
        assertEquals(SocialConstants.CLAN_ID_NULL_MESSAGE, ex1.getMessage(), MSG_EXCEPTION_MSG);

        IllegalStateException ex2 = assertThrows(IllegalStateException.class, () ->
            validator.requireClanId("   "),
            "Should throw IllegalStateException on blank clanId"
        );
        assertEquals(SocialConstants.CLAN_ID_NULL_MESSAGE, ex2.getMessage(), MSG_EXCEPTION_MSG);
    }

    @Test
    void requireUsername_Valid_DoesNotThrow() {
        validator.requireUsername(USERNAME);
    }

    @Test
    void requireUsername_NullOrBlank_ThrowsIllegalStateException() {
        IllegalStateException ex1 = assertThrows(IllegalStateException.class, () ->
            validator.requireUsername(null),
            "Should throw IllegalStateException on null username"
        );
        assertEquals(SocialConstants.USER_ID_NULL_MESSAGE, ex1.getMessage(), MSG_EXCEPTION_MSG);

        IllegalStateException ex2 = assertThrows(IllegalStateException.class, () ->
            validator.requireUsername(""),
            "Should throw IllegalStateException on empty username"
        );
        assertEquals(SocialConstants.USER_ID_NULL_MESSAGE, ex2.getMessage(), MSG_EXCEPTION_MSG);
    }

    @Test
    void requireValidClanName_Valid_DoesNotThrow() {
        validator.requireValidClanName("Normal Name");
    }

    @Test
    void requireValidClanName_NullOrBlank_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () ->
            validator.requireValidClanName(null),
            "Should throw IllegalArgumentException on null clanName"
        );
        assertEquals("Clan name cannot be empty", ex1.getMessage(), MSG_EXCEPTION_MSG);

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
            validator.requireValidClanName(""),
            "Should throw IllegalArgumentException on empty clanName"
        );
        assertEquals("Clan name cannot be empty", ex2.getMessage(), MSG_EXCEPTION_MSG);
    }

    @Test
    void requireValidClanName_TooLong_ThrowsIllegalArgumentException() {
        String longName = "a".repeat(101);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            validator.requireValidClanName(longName),
            "Should throw IllegalArgumentException on too long clanName"
        );
        assertEquals("Clan name exceeds maximum length of 100", ex.getMessage(), MSG_EXCEPTION_MSG);
    }

    @Test
    void requireValidClanDescription_Valid_DoesNotThrow() {
        validator.requireValidClanDescription(null);
        validator.requireValidClanDescription("Normal description");
    }

    @Test
    void requireValidClanDescription_TooLong_ThrowsIllegalArgumentException() {
        String longDesc = "a".repeat(256);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            validator.requireValidClanDescription(longDesc),
            "Should throw IllegalArgumentException on too long description"
        );
        assertEquals("Clan description exceeds maximum length of 255", ex.getMessage(), MSG_EXCEPTION_MSG);
    }

    @Test
    void requireClanNameAvailable_Available_DoesNotThrow() {
        validator.requireClanNameAvailable(false);
    }

    @Test
    void requireClanNameAvailable_NotAvailable_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            validator.requireClanNameAvailable(true),
            "Should throw IllegalArgumentException when name is already used"
        );
        assertEquals(SocialConstants.CLAN_NAME_ALREADY_USED_MESSAGE, ex.getMessage(), MSG_EXCEPTION_MSG);
    }

    @Test
    void requireLeaderPrivilege_IsLeader_DoesNotThrow() {
        Clan clan = new Clan();
        clan.setLeaderUsername(USERNAME);
        validator.requireLeaderPrivilege(clan, USERNAME, ERROR_MSG);
    }

    @Test
    void requireLeaderPrivilege_IsNotLeader_ThrowsIllegalStateException() {
        Clan clan = new Clan();
        clan.setLeaderUsername(USERNAME);
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            validator.requireLeaderPrivilege(clan, OTHER_USER, ERROR_MSG),
            "Should throw IllegalStateException when user is not leader"
        );
        assertEquals(ERROR_MSG, ex.getMessage(), MSG_EXCEPTION_MSG);
    }

    @Test
    void requireNotAlreadyMember_NotMember_DoesNotThrow() {
        validator.requireNotAlreadyMember(false);
    }

    @Test
    void requireNotAlreadyMember_IsMember_ThrowsIllegalStateException() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            validator.requireNotAlreadyMember(true),
            "Should throw IllegalStateException when user is already a member"
        );
        assertEquals(SocialConstants.ALREADY_MEMBER_MESSAGE, ex.getMessage(), MSG_EXCEPTION_MSG);
    }

    @Test
    void requireNotMemberOfOtherClan_NotInOtherClan_DoesNotThrow() {
        validator.requireNotMemberOfOtherClan(false);
    }

    @Test
    void requireNotMemberOfOtherClan_InOtherClan_ThrowsIllegalStateException() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            validator.requireNotMemberOfOtherClan(true),
            "Should throw IllegalStateException when user is member of another clan"
        );
        assertEquals(SocialConstants.ALREADY_IN_OTHER_CLAN_MESSAGE, ex.getMessage(), MSG_EXCEPTION_MSG);
    }

    @Test
    void requireClanNotFull_NotFull_DoesNotThrow() {
        validator.requireClanNotFull(10);
    }

    @Test
    void requireClanNotFull_Full_ThrowsIllegalStateException() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            validator.requireClanNotFull(50), // Max is 50
            "Should throw IllegalStateException when clan is full"
        );
        assertEquals(SocialConstants.CLAN_FULL_MESSAGE, ex.getMessage(), MSG_EXCEPTION_MSG);
    }

    @Test
    void resolveReplacementLeader_Success() {
        ClanMember member1 = new ClanMember();
        member1.setUsername(USERNAME);
        ClanMember member2 = new ClanMember();
        member2.setUsername(OTHER_MEMBER());

        String replacement = validator.resolveReplacementLeader(List.of(member1, member2), USERNAME);
        assertEquals(OTHER_MEMBER(), replacement, MSG_REPLACEMENT_LEADER);
    }

    @Test
    void resolveReplacementLeader_NoReplacement_ThrowsIllegalStateException() {
        ClanMember member1 = new ClanMember();
        member1.setUsername(USERNAME);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            validator.resolveReplacementLeader(List.of(member1), USERNAME),
            "Should throw IllegalStateException when no replacement leader found"
        );
        assertEquals(SocialConstants.FAILED_FIND_REPLACEMENT_LEADER_MESSAGE, ex.getMessage(), MSG_EXCEPTION_MSG);
    }

    private String OTHER_MEMBER() {
        return OTHER_USER;
    }
}
