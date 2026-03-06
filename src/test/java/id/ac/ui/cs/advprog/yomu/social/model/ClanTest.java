package id.ac.ui.cs.advprog.yomu.social.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClanTest {

    private Clan clan;

    @BeforeEach
    void setUp() {
        this.clan = new Clan();
    }

    @Test
    void testSetAndGetId() {
        String id = "uuid-test-123";
        clan.setId(id);
        assertEquals(id, clan.getId(), "The Clan ID should match the assigned UUID");
    }

    @Test
    void testSetAndGetName() {
        String name = "Clan Harapan Bangsa";
        clan.setName(name);
        assertEquals(name, clan.getName(), "The Clan name should match the assigned name");
    }

    @Test
    void testSetAndGetDescription() {
        String desc = "Tempat berkumpulnya para pembaca aktif.";
        clan.setDescription(desc);
        assertEquals(desc, clan.getDescription(), "The Clan description should match the assigned text");
    }

    @Test
    void testSetAndGetLeaderUserId() {
        String leaderId = "user-pro-99";
        clan.setLeaderUserId(leaderId);
        assertEquals(leaderId, clan.getLeaderUserId(), "The Leader User ID should match the assigned user ID");
    }

    @Test
    void testClanCreationWithValues() {
        clan.setId("1");
        clan.setName("Test Clan");
        clan.setDescription("Test Desc");
        clan.setLeaderUserId("Leader1");

        assertAll("Verify all Clan fields after manual setting",
            () -> assertEquals("1", clan.getId(), "ID verification failed"),
            () -> assertEquals("Test Clan", clan.getName(), "Name verification failed"),
            () -> assertEquals("Test Desc", clan.getDescription(), "Description verification failed"),
            () -> assertEquals("Leader1", clan.getLeaderUserId(), "Leader User ID verification failed")
        );
    }
}