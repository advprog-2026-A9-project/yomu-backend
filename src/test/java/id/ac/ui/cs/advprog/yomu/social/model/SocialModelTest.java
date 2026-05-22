package id.ac.ui.cs.advprog.yomu.social.model;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class SocialModelTest {

    @Test
    void testTierPromotionAndDemotion() {
        assertAll("Verify tier promotion, demotion, levels and display names",
                () -> assertEquals(Tier.SILVER, Tier.BRONZE.promote(), "Bronze promote should be Silver"),
                () -> assertEquals(Tier.GOLD, Tier.SILVER.promote(), "Silver promote should be Gold"),
                () -> assertEquals(Tier.DIAMOND, Tier.GOLD.promote(), "Gold promote should be Diamond"),
                () -> assertEquals(Tier.DIAMOND, Tier.DIAMOND.promote(), "Diamond promote should remain Diamond"),
                () -> assertEquals(Tier.GOLD, Tier.DIAMOND.demote(), "Diamond demote should be Gold"),
                () -> assertEquals(Tier.SILVER, Tier.GOLD.demote(), "Gold demote should be Silver"),
                () -> assertEquals(Tier.BRONZE, Tier.SILVER.demote(), "Silver demote should be Bronze"),
                () -> assertEquals(Tier.BRONZE, Tier.BRONZE.demote(), "Bronze demote should remain Bronze"),
                () -> assertEquals(1, Tier.BRONZE.getLevel(), "Bronze level should be 1"),
                () -> assertEquals("Bronze", Tier.BRONZE.getDisplayName(), "Bronze display name should be 'Bronze'"));
    }

    @Test
    void testSeasonState() {
        SeasonState state = new SeasonState();
        state.setId(10L);
        state.setSeasonNumber(5);
        state.setActive(false);
        LocalDateTime now = LocalDateTime.now();
        state.setCreatedAt(now);
        state.setUpdatedAt(now);

        assertAll("Verify season state getter/setter properties",
                () -> assertEquals(10L, state.getId(), "ID should match"),
                () -> assertEquals(5, state.getSeasonNumber(), "Season number should match"),
                () -> assertFalse(state.isActive(), "Active flag should match"),
                () -> assertEquals(now, state.getCreatedAt(), "CreatedAt timestamp should match"),
                () -> assertEquals(now, state.getUpdatedAt(), "UpdatedAt timestamp should match"));
    }

    @Test
    void testClanJoinRequest() {
        ClanJoinRequest req = new ClanJoinRequest();
        req.setId(1L);
        req.setClanId("clan-1");
        req.setUsername("user-1");
        req.setStatus(ClanJoinRequestStatus.PENDING);

        assertAll("Verify clan join request lifecycle",
                () -> assertNotNull(req.getCreatedAt(), "CreatedAt should be auto-initialized"),
                () -> {
                    req.accept();
                    assertEquals(ClanJoinRequestStatus.ACCEPTED, req.getStatus(), "Status should be ACCEPTED after accept");
                    assertThrows(IllegalStateException.class, req::accept, "Accepting already accepted request should throw IllegalStateException");
                },
                () -> {
                    req.setStatus(ClanJoinRequestStatus.PENDING);
                    req.reject();
                    assertEquals(ClanJoinRequestStatus.REJECTED, req.getStatus(), "Status should be REJECTED after reject");
                    assertThrows(IllegalStateException.class, req::reject, "Rejecting already rejected request should throw IllegalStateException");
                });
    }

    @Test
    void testClanModifier() {
        ClanModifier mod = new ClanModifier();
        mod.setId(100L);
        mod.setClanId("clan-abc");
        mod.setKey("key-1");
        mod.setMultiplier(1.5);
        mod.setActive(true);
        Instant start = Instant.now();
        mod.setStartAt(start);
        mod.setEndAt(start);

        assertAll("Verify clan modifier properties and type logic",
                () -> {
                    mod.setType(ModifierType.BUFF);
                    assertTrue(mod.isBuff(), "Should be a buff");
                    assertFalse(mod.isDebuff(), "Should not be a debuff");
                },
                () -> {
                    mod.setType(ModifierType.DEBUFF);
                    assertFalse(mod.isBuff(), "Should not be a buff");
                    assertTrue(mod.isDebuff(), "Should be a debuff");
                },
                () -> assertEquals(100L, mod.getId(), "ID should match"),
                () -> assertEquals("clan-abc", mod.getClanId(), "Clan ID should match"),
                () -> assertEquals("key-1", mod.getKey(), "Key should match"),
                () -> assertEquals(1.5, mod.getMultiplier(), "Multiplier should match"),
                () -> assertTrue(mod.isActive(), "Active flag should match"),
                () -> assertEquals(start, mod.getStartAt(), "Start time should match"),
                () -> assertEquals(start, mod.getEndAt(), "End time should match"));
    }

    @Test
    void testClanQuizStats() {
        ClanQuizStats stats = new ClanQuizStats();
        stats.setClanId("clan-123");
        stats.setTotalQuizAttempts(10);
        stats.setTotalCorrectAnswers(8);
        stats.setTotalQuestions(15);
        stats.setTotalScore(1000);
        stats.setRollingQuizHistory("history");

        assertAll("Verify clan quiz stats properties",
                () -> assertEquals("clan-123", stats.getClanId(), "Clan ID should match"),
                () -> assertEquals(10, stats.getTotalQuizAttempts(), "Total attempts should match"),
                () -> assertEquals(8, stats.getTotalCorrectAnswers(), "Total correct answers should match"),
                () -> assertEquals(15, stats.getTotalQuestions(), "Total questions should match"),
                () -> assertEquals(1000, stats.getTotalScore(), "Total score should match"),
                () -> assertEquals("history", stats.getRollingQuizHistory(), "Rolling history should match"));
    }

    @Test
    void testEnumsAndConstants() {
        assertAll("Verify enum parsing values",
                () -> assertNotNull(ClanRole.valueOf("LEADER"), "LEADER role should exist"),
                () -> assertNotNull(ClanJoinRequestStatus.valueOf("PENDING"), "PENDING request status should exist"),
                () -> assertNotNull(ClanTier.valueOf("BRONZE"), "BRONZE tier should exist"),
                () -> assertNotNull(ModifierType.valueOf("BUFF"), "BUFF modifier type should exist"));
    }
}
