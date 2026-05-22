package id.ac.ui.cs.advprog.yomu.social.validation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;

class ClanValidatorImplTest {

    private static final String LEADER_USER = "leader";
    private static final String CLAN_NAME_EMPTY_ERR = "Clan name cannot be empty";

    private ClanValidatorImpl validator;

    @BeforeEach
    void setUp() {
        validator = new ClanValidatorImpl();
    }

    @Test
    void requireClanId_WhenValid_ShouldDoNothing() {
        assertDoesNotThrow(() -> validator.requireClanId("clan-123"),
                "Should not throw exception when clan ID is valid");
    }

    @Test
    void requireClanId_WhenNullOrBlank_ShouldThrowException() {
        assertAll("Verify requireClanId validation errors",
                () -> {
                    IllegalStateException exNull = assertThrows(IllegalStateException.class,
                            () -> validator.requireClanId(null),
                            "Should throw IllegalStateException on null clan ID");
                    assertEquals(SocialConstants.CLAN_ID_NULL_MESSAGE, exNull.getMessage(), "Message should match CLAN_ID_NULL_MESSAGE");
                },
                () -> {
                    IllegalStateException exBlank = assertThrows(IllegalStateException.class,
                            () -> validator.requireClanId("   "),
                            "Should throw IllegalStateException on blank clan ID");
                    assertEquals(SocialConstants.CLAN_ID_NULL_MESSAGE, exBlank.getMessage(), "Message should match CLAN_ID_NULL_MESSAGE");
                });
    }

    @Test
    void requireUsername_WhenValid_ShouldDoNothing() {
        assertDoesNotThrow(() -> validator.requireUsername("user-123"),
                "Should not throw exception when username is valid");
    }

    @Test
    void requireUsername_WhenNullOrBlank_ShouldThrowException() {
        assertAll("Verify requireUsername validation errors",
                () -> {
                    IllegalStateException exNull = assertThrows(IllegalStateException.class,
                            () -> validator.requireUsername(null),
                            "Should throw IllegalStateException on null username");
                    assertEquals(SocialConstants.USER_ID_NULL_MESSAGE, exNull.getMessage(), "Message should match USER_ID_NULL_MESSAGE");
                },
                () -> {
                    IllegalStateException exBlank = assertThrows(IllegalStateException.class,
                            () -> validator.requireUsername("   "),
                            "Should throw IllegalStateException on blank username");
                    assertEquals(SocialConstants.USER_ID_NULL_MESSAGE, exBlank.getMessage(), "Message should match USER_ID_NULL_MESSAGE");
                });
    }

    @Test
    void requireValidClanName_WhenValid_ShouldDoNothing() {
        assertDoesNotThrow(() -> validator.requireValidClanName("Yomu"),
                "Should not throw exception when clan name is valid");
    }

    @Test
    void requireValidClanName_WhenNullOrBlank_ShouldThrowException() {
        assertAll("Verify requireValidClanName null and blank errors",
                () -> {
                    IllegalArgumentException exNull = assertThrows(IllegalArgumentException.class,
                            () -> validator.requireValidClanName(null),
                            "Should throw IllegalArgumentException on null clan name");
                    assertEquals(CLAN_NAME_EMPTY_ERR, exNull.getMessage(), "Message should match empty name error");
                },
                () -> {
                    IllegalArgumentException exBlank = assertThrows(IllegalArgumentException.class,
                            () -> validator.requireValidClanName("   "),
                            "Should throw IllegalArgumentException on blank clan name");
                    assertEquals(CLAN_NAME_EMPTY_ERR, exBlank.getMessage(), "Message should match empty name error");
                });
    }

    @Test
    void requireValidClanName_WhenTooLong_ShouldThrowException() {
        String longName = "a".repeat(101);
        assertAll("Verify requireValidClanName length validation",
                () -> {
                    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                            () -> validator.requireValidClanName(longName),
                            "Should throw IllegalArgumentException on too long name");
                    assertEquals("Clan name exceeds maximum length of 100", ex.getMessage(), "Message should match max length error");
                });
    }

    @Test
    void requireValidClanDescription_WhenValid_ShouldDoNothing() {
        assertAll("Verify requireValidClanDescription valid properties",
                () -> assertDoesNotThrow(() -> validator.requireValidClanDescription("description"), "Should not throw exception for valid description"),
                () -> assertDoesNotThrow(() -> validator.requireValidClanDescription(null), "Should not throw exception for null description"));
    }

    @Test
    void requireValidClanDescription_WhenTooLong_ShouldThrowException() {
        String longDesc = "a".repeat(256);
        assertAll("Verify requireValidClanDescription length validation",
                () -> {
                    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                            () -> validator.requireValidClanDescription(longDesc),
                            "Should throw IllegalArgumentException on too long description");
                    assertEquals("Clan description exceeds maximum length of 255", ex.getMessage(), "Message should match max description length error");
                });
    }

    @Test
    void requireClanNameAvailable_WhenAvailable_ShouldDoNothing() {
        assertDoesNotThrow(() -> validator.requireClanNameAvailable(false),
                "Should not throw exception when clan name is available");
    }

    @Test
    void requireClanNameAvailable_WhenTaken_ShouldThrowException() {
        assertAll("Verify requireClanNameAvailable taken error",
                () -> {
                    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                            () -> validator.requireClanNameAvailable(true),
                            "Should throw IllegalArgumentException when name is taken");
                    assertEquals(SocialConstants.CLAN_NAME_ALREADY_USED_MESSAGE, ex.getMessage(), "Message should match CLAN_NAME_ALREADY_USED_MESSAGE");
                });
    }

    @Test
    void requireLeaderPrivilege_WhenLeader_ShouldDoNothing() {
        Clan clan = new Clan();
        clan.setLeaderUsername(LEADER_USER);
        assertDoesNotThrow(() -> validator.requireLeaderPrivilege(clan, LEADER_USER, "err"),
                "Should not throw exception when user has leader privilege");
    }

    @Test
    void requireLeaderPrivilege_WhenNotLeader_ShouldThrowException() {
        Clan clan = new Clan();
        clan.setLeaderUsername(LEADER_USER);
        assertAll("Verify leader privilege enforcement",
                () -> {
                    IllegalStateException ex = assertThrows(IllegalStateException.class,
                            () -> validator.requireLeaderPrivilege(clan, "member", "err"),
                            "Should throw IllegalStateException when user is not leader");
                    assertEquals("err", ex.getMessage(), "Message should match specified custom error message");
                });
    }

    @Test
    void requireNotAlreadyMember_WhenNotMember_ShouldDoNothing() {
        assertDoesNotThrow(() -> validator.requireNotAlreadyMember(false),
                "Should not throw exception when user is not already member");
    }

    @Test
    void requireNotAlreadyMember_WhenAlreadyMember_ShouldThrowException() {
        assertAll("Verify already member validation error",
                () -> {
                    IllegalStateException ex = assertThrows(IllegalStateException.class,
                            () -> validator.requireNotAlreadyMember(true),
                            "Should throw IllegalStateException when user is already a member");
                    assertEquals(SocialConstants.ALREADY_MEMBER_MESSAGE, ex.getMessage(), "Message should match ALREADY_MEMBER_MESSAGE");
                });
    }

    @Test
    void requireNotMemberOfOtherClan_WhenNotMemberOfOtherClan_ShouldDoNothing() {
        assertDoesNotThrow(() -> validator.requireNotMemberOfOtherClan(false),
                "Should not throw exception when user is not member of another clan");
    }

    @Test
    void requireNotMemberOfOtherClan_WhenMemberOfOtherClan_ShouldThrowException() {
        assertAll("Verify member of other clan validation error",
                () -> {
                    IllegalStateException ex = assertThrows(IllegalStateException.class,
                            () -> validator.requireNotMemberOfOtherClan(true),
                            "Should throw IllegalStateException when user is in another clan");
                    assertEquals(SocialConstants.ALREADY_IN_OTHER_CLAN_MESSAGE, ex.getMessage(), "Message should match ALREADY_IN_OTHER_CLAN_MESSAGE");
                });
    }

    @Test
    void requireClanNotFull_WhenNotFull_ShouldDoNothing() {
        assertDoesNotThrow(() -> validator.requireClanNotFull(49),
                "Should not throw exception when clan size is under maximum size");
    }

    @Test
    void requireClanNotFull_WhenFull_ShouldThrowException() {
        assertAll("Verify clan full validation error",
                () -> {
                    IllegalStateException ex = assertThrows(IllegalStateException.class,
                            () -> validator.requireClanNotFull(50),
                            "Should throw IllegalStateException when clan is full");
                    assertEquals(SocialConstants.CLAN_FULL_MESSAGE, ex.getMessage(), "Message should match CLAN_FULL_MESSAGE");
                });
    }

    @Test
    void resolveReplacementLeader_WhenOtherMemberExists_ShouldReturnUsername() {
        ClanMember currentLeader = new ClanMember();
        currentLeader.setUsername(LEADER_USER);

        ClanMember member = new ClanMember();
        member.setUsername("member");

        String replacement = validator.resolveReplacementLeader(List.of(currentLeader, member), LEADER_USER);
        assertEquals("member", replacement, "Should return member username as replacement leader");
    }

    @Test
    void resolveReplacementLeader_WhenNoOtherMemberExists_ShouldThrowException() {
        ClanMember currentLeader = new ClanMember();
        currentLeader.setUsername(LEADER_USER);

        assertAll("Verify resolve replacement leader empty error",
                () -> {
                    IllegalStateException ex = assertThrows(IllegalStateException.class,
                            () -> validator.resolveReplacementLeader(List.of(currentLeader), LEADER_USER),
                            "Should throw IllegalStateException when no other member exists");
                    assertEquals(SocialConstants.FAILED_FIND_REPLACEMENT_LEADER_MESSAGE, ex.getMessage(), "Message should match FAILED_FIND_REPLACEMENT_LEADER_MESSAGE");
                });
    }
}
