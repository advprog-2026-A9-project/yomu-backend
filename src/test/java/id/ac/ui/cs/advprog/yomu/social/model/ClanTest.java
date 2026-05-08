package id.ac.ui.cs.advprog.yomu.social.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClanTest {

    private Clan clan;

    @BeforeEach
    void setUp() {
        clan = new Clan();
        clan.setTier(Tier.SILVER);
        clan.setScore(100);
    }

    @Test
    void testPromote_ShouldIncreaseTierAndResetScore() {
        clan.promote();
        assertAll("Verify promote from SILVER to GOLD",
                () -> assertEquals(Tier.GOLD, clan.getTier(), "Tier should be promoted to GOLD"),
                () -> assertEquals(0, clan.getScore(), "Score should be reset to 0 after promotion"));
    }

    @Test
    void testPromote_FromDiamond_ShouldStayDiamondAndResetScore() {
        clan.setTier(Tier.DIAMOND);
        clan.promote();
        assertAll("Verify promote from DIAMOND stays DIAMOND",
                () -> assertEquals(Tier.DIAMOND, clan.getTier(), "Tier should remain DIAMOND"),
                () -> assertEquals(0, clan.getScore(), "Score should be reset to 0 even if tier doesn't change"));
    }

    @Test
    void testDemote_ShouldDecreaseTierAndResetScore() {
        clan.demote();
        assertAll("Verify demote from SILVER to BRONZE",
                () -> assertEquals(Tier.BRONZE, clan.getTier(), "Tier should be demoted to BRONZE"),
                () -> assertEquals(0, clan.getScore(), "Score should be reset to 0 after demotion"));
    }

    @Test
    void testDemote_FromBronze_ShouldStayBronzeAndResetScore() {
        clan.setTier(Tier.BRONZE);
        clan.demote();
        assertAll("Verify demote from BRONZE stays BRONZE",
                () -> assertEquals(Tier.BRONZE, clan.getTier(), "Tier should remain BRONZE"),
                () -> assertEquals(0, clan.getScore(), "Score should be reset to 0 even if tier doesn't change"));
    }
}
