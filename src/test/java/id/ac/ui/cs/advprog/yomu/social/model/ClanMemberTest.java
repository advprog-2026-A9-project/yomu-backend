package id.ac.ui.cs.advprog.yomu.social.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClanMemberTest {

    private ClanMember clanMember;

    @BeforeEach
    void setUp() {
        this.clanMember = new ClanMember();
    }

    @Test
    void testSetAndGetId() {
        Long id = 1L;
        clanMember.setId(id);
        assertEquals(id, clanMember.getId(), "The ID should match the assigned value");
    }

    @Test
    void testSetAndGetClanId() {
        String clanId = "clan-uuid-001";
        clanMember.setClanId(clanId);
        assertEquals(clanId, clanMember.getClanId(), "The Clan ID should match the assigned value");
    }

    @Test
    void testSetAndGetUsername() {
        String username = "user-uuid-999";
        clanMember.setUsername(username);
        assertEquals(username, clanMember.getUsername(), "The Username should match the assigned value");
    }

    @Test
    void testClanMemberFieldsNotNull() {
        clanMember.setId(10L);
        clanMember.setClanId("C-1");
        clanMember.setUsername("U-1");

        assertAll("Verify ClanMember Fields",
                () -> assertNotNull(clanMember.getId(), "ClanMember ID should not be null"),
                () -> assertNotNull(clanMember.getClanId(), "Clan ID should not be null"),
                () -> assertNotNull(clanMember.getUsername(), "Username should not be null"));
    }
}