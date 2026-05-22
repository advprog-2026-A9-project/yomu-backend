package id.ac.ui.cs.advprog.yomu.social.mapper;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import id.ac.ui.cs.advprog.yomu.social.dto.ClanDetailResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanLeaderboardRow;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanMemberDTO;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanModifierDTO;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryRow;
import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardEntryResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.MyClanResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.SeasonClanSummary;
import id.ac.ui.cs.advprog.yomu.social.dto.SeasonEndResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.SeasonStatusResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.SeasonTierSummary;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.model.ClanRole;
import id.ac.ui.cs.advprog.yomu.social.model.ClanModifier;
import id.ac.ui.cs.advprog.yomu.social.model.ModifierType;
import id.ac.ui.cs.advprog.yomu.social.model.SeasonState;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;

@SuppressWarnings("PMD")
class SocialMapperImplTest {

    private static final String CLAN_ID = "clan-123";
    private static final String CLAN_NAME = "Wibu Indo";
    private static final String CLAN_DESC = "Wibu united";
    private static final String LEADER = "leader-username";
    private static final String MEMBER_USER = "member-username";
    private static final String MODIFIER_KEY = id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants.DAILY_MISSION_BUFF_KEY;

    // Assertion messages
    private static final String MSG_ID = "Id should match";
    private static final String MSG_NAME = "Name should match";
    private static final String MSG_DESC = "Description should match";
    private static final String MSG_LEADER = "Leader username should match";
    private static final String MSG_TIER = "Tier should match";
    private static final String MSG_SCORE = "Score should match";
    private static final String MSG_RANK = "Rank should match";
    private static final String MSG_ROLE = "Role should match";
    private static final String MSG_MEMBER_COUNT = "Member count should match";
    private static final String MSG_BUFFS = "Buffs list should match";
    private static final String MSG_DEBUFFS = "Debuffs list should match";
    private static final String MSG_DISPLAY_NAME = "Display name should match";
    private static final String MSG_MULTIPLIER = "Multiplier should match";
    private static final String MSG_SEASON_NUM = "Season number should match";
    private static final String MSG_SEASON_STATUS = "Season status should match";

    private SocialMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new SocialMapperImpl();
    }

    @Test
    void toClanSummaryResponse_ShouldMapCorrectly() {
        ClanSummaryRow row = new ClanSummaryRow() {
            public String getClanId() { return CLAN_ID; }
            public String getClanName() { return CLAN_NAME; }
            public String getDescription() { return CLAN_DESC; }
            public String getLeaderUsername() { return LEADER; }
            public Tier getTier() { return Tier.BRONZE; }
            public int getScore() { return 120; }
            public long getMemberCount() { return 5L; }
        };

        ClanModifierDTO buff = new ClanModifierDTO("Daily Mission Buff", "x1.20", "buff", "Active", "Description");
        List<ClanModifierDTO> activeBuffs = List.of(buff);
        List<ClanModifierDTO> debuffs = List.of();

        ClanSummaryResponse response = mapper.toClanSummaryResponse(row, activeBuffs, debuffs, 144);

        assertAll("Verify toClanSummaryResponse mapping",
                () -> assertEquals(CLAN_ID, response.id(), MSG_ID),
                () -> assertEquals(CLAN_NAME, response.name(), MSG_NAME),
                () -> assertEquals(CLAN_DESC, response.description(), MSG_DESC),
                () -> assertEquals(LEADER, response.leaderUsername(), MSG_LEADER),
                () -> assertEquals(Tier.BRONZE.name(), response.tier(), MSG_TIER),
                () -> assertEquals(120, response.score(), MSG_SCORE),
                () -> assertEquals(144, response.effectiveScore(), "Effective score should match"),
                () -> assertEquals(5L, response.memberCount(), MSG_MEMBER_COUNT),
                () -> assertEquals(activeBuffs, response.activeBuffs(), MSG_BUFFS),
                () -> assertEquals(debuffs, response.debuffs(), MSG_DEBUFFS)
        );
    }

    @Test
    void toClanDetailResponse_ShouldMapCorrectly() {
        Clan clan = new Clan();
        clan.setId(CLAN_ID);
        clan.setName(CLAN_NAME);
        clan.setDescription(CLAN_DESC);
        clan.setLeaderUsername(LEADER);
        clan.setTier(Tier.GOLD);
        clan.setScore(500);

        ClanMemberDTO memberDTO = new ClanMemberDTO(MEMBER_USER, "MEMBER", 0, 0, true);
        List<ClanMemberDTO> memberDTOs = List.of(memberDTO);
        List<ClanModifierDTO> activeBuffs = List.of();
        List<ClanModifierDTO> debuffs = List.of();

        ClanDetailResponse response = mapper.toClanDetailResponse(clan, 3, 1, memberDTOs, activeBuffs, debuffs);

        assertAll("Verify toClanDetailResponse mapping",
                () -> assertEquals(CLAN_ID, response.id(), MSG_ID),
                () -> assertEquals(CLAN_NAME, response.name(), MSG_NAME),
                () -> assertEquals(CLAN_DESC, response.description(), MSG_DESC),
                () -> assertEquals(LEADER, response.leaderUsername(), MSG_LEADER),
                () -> assertEquals(Tier.GOLD.name(), response.tier(), MSG_TIER),
                () -> assertEquals(3, response.rank(), MSG_RANK),
                () -> assertEquals(500, response.score(), MSG_SCORE),
                () -> assertEquals(1, response.memberCount(), MSG_MEMBER_COUNT),
                () -> assertEquals(50, response.maxMembers(), "Max members should match"),
                () -> assertEquals(0.0, response.avgAccuracy(), "Avg accuracy should be 0.0"),
                () -> assertEquals(memberDTOs, response.members(), "Members list should match"),
                () -> assertEquals(activeBuffs, response.activeBuffs(), MSG_BUFFS),
                () -> assertEquals(debuffs, response.debuffs(), MSG_DEBUFFS)
        );
    }

    @Test
    void toClanDetailResponse_WhenDescriptionIsNull_ShouldMapToEmptyString() {
        Clan clan = new Clan();
        clan.setId(CLAN_ID);
        clan.setName(CLAN_NAME);
        clan.setDescription(null);
        clan.setLeaderUsername(LEADER);
        clan.setTier(Tier.GOLD);
        clan.setScore(500);

        ClanDetailResponse response = mapper.toClanDetailResponse(clan, 3, 1, List.of(), List.of(), List.of());

        assertAll("Verify toClanDetailResponse mapping when description is null",
                () -> assertEquals("", response.description(), "Null description should map to empty string")
        );
    }

    @Test
    void toMyClanResponse_ShouldMapCorrectly() {
        Clan clan = new Clan();
        clan.setId(CLAN_ID);
        clan.setName(CLAN_NAME);
        clan.setDescription(CLAN_DESC);
        clan.setLeaderUsername(LEADER);
        clan.setTier(Tier.GOLD);
        clan.setScore(350);

        ClanMember member = new ClanMember();
        member.setUsername(MEMBER_USER);
        member.setRole(ClanRole.MEMBER);
        List<ClanMember> members = List.of(member);

        MyClanResponse response = mapper.toMyClanResponse(clan, "MEMBER", 5, members);

        assertAll("Verify toMyClanResponse mapping",
                () -> assertEquals(CLAN_ID, response.id(), MSG_ID),
                () -> assertEquals(CLAN_NAME, response.name(), MSG_NAME),
                () -> assertEquals(CLAN_DESC, response.description(), MSG_DESC),
                () -> assertEquals(LEADER, response.leaderUsername(), MSG_LEADER),
                () -> assertEquals("MEMBER", response.role(), MSG_ROLE),
                () -> assertEquals(Tier.GOLD.getDisplayName(), response.tier(), MSG_TIER),
                () -> assertEquals(350, response.score(), MSG_SCORE),
                () -> assertEquals(5, response.rank(), MSG_RANK),
                () -> assertEquals(members, response.members(), "Members list should match")
        );
    }

    @Test
    void toClanMemberDTO_ShouldMapCorrectly() {
        ClanMember member = new ClanMember();
        member.setUsername(MEMBER_USER);
        member.setRole(ClanRole.MEMBER);

        ClanMemberDTO dto = mapper.toClanMemberDTO(member);

        assertAll("Verify toClanMemberDTO mapping",
                () -> assertEquals(MEMBER_USER, dto.username(), "Username should match"),
                () -> assertEquals("MEMBER", dto.role(), "Role should match"),
                () -> assertEquals(0, dto.contribution(), "Contribution should be 0"),
                () -> assertEquals(0, dto.streak(), "Streak should be 0"),
                () -> assertTrue(dto.isOnline(), "Online should be true")
        );
    }

    @Test
    void toClanModifierDTO_WhenDailyMissionBuff_ShouldMapCorrectly() {
        ClanModifier modifier = new ClanModifier();
        modifier.setKey(MODIFIER_KEY);
        modifier.setMultiplier(1.20);
        modifier.setType(ModifierType.BUFF);
        modifier.setEndAt(null);

        ClanModifierDTO dto = mapper.toClanModifierDTO(modifier);

        assertAll("Verify toClanModifierDTO for daily mission buff",
                () -> assertEquals("Daily Mission Buff", dto.name(), MSG_DISPLAY_NAME),
                () -> assertEquals("x1.20", dto.multiplier(), MSG_MULTIPLIER),
                () -> assertEquals("buff", dto.type(), "Type should be buff"),
                () -> assertEquals("Active", dto.duration(), "Duration should show active when EndAt is null"),
                () -> assertEquals("+20% Points Multiplier", dto.description(), MSG_DESC)
        );
    }

    @Test
    void toClanModifierDTO_WhenLowAccuracyPenalty_ShouldMapCorrectly() {
        Instant endTime = Instant.parse("2026-05-22T20:00:00Z");
        ClanModifier modifier = new ClanModifier();
        modifier.setKey(id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants.LOW_ACCURACY_PENALTY_KEY);
        modifier.setMultiplier(0.80);
        modifier.setType(ModifierType.DEBUFF);
        modifier.setEndAt(endTime);

        ClanModifierDTO dto = mapper.toClanModifierDTO(modifier);

        assertAll("Verify toClanModifierDTO for low accuracy penalty",
                () -> assertEquals("Low Accuracy Penalty", dto.name(), MSG_DISPLAY_NAME),
                () -> assertEquals("x0.80", dto.multiplier(), MSG_MULTIPLIER),
                () -> assertEquals("debuff", dto.type(), "Type should be debuff"),
                () -> assertEquals("Until 2026-05-22T20:00:00Z", dto.duration(), "Duration should show end time"),
                () -> assertEquals("-20% Points Multiplier", dto.description(), MSG_DESC)
        );
    }

    @Test
    void toClanModifierDTO_WhenOtherModifier_ShouldFallbackToGenericInfo() {
        ClanModifier modifier = new ClanModifier();
        modifier.setKey("custom_mod");
        modifier.setMultiplier(1.50);
        modifier.setType(ModifierType.BUFF);
        modifier.setEndAt(null);

        ClanModifierDTO dto = mapper.toClanModifierDTO(modifier);

        assertAll("Verify fallback mapping for custom modifier",
                () -> assertEquals("custom_mod", dto.name(), MSG_DISPLAY_NAME),
                () -> assertEquals("BUFF modifier", dto.description(), MSG_DESC)
        );
    }

    @Test
    void toLeaderboardEntryResponse_FromRow_ShouldMapCorrectly() {
        ClanLeaderboardRow row = new ClanLeaderboardRow() {
            public String getClanId() { return CLAN_ID; }
            public String getClanName() { return CLAN_NAME; }
            public Tier getTier() { return Tier.DIAMOND; }
            public int getScore() { return 1000; }
            public long getMemberCount() { return 12L; }
        };

        LeaderboardEntryResponse entry = mapper.toLeaderboardEntryResponse(row, 2);

        assertAll("Verify LeaderboardEntryResponse from row",
                () -> assertEquals(CLAN_ID, entry.clanId(), MSG_ID),
                () -> assertEquals(CLAN_NAME, entry.clanName(), MSG_NAME),
                () -> assertEquals(Tier.DIAMOND.getDisplayName(), entry.tier(), MSG_TIER),
                () -> assertEquals(1000, entry.score(), MSG_SCORE),
                () -> assertEquals(2, entry.rank(), MSG_RANK),
                () -> assertEquals(12, entry.memberCount(), MSG_MEMBER_COUNT)
        );
    }

    @Test
    void toLeaderboardEntryResponse_FromClan_ShouldMapCorrectly() {
        Clan clan = new Clan();
        clan.setId(CLAN_ID);
        clan.setName(CLAN_NAME);
        clan.setTier(Tier.DIAMOND);
        clan.setScore(999);

        LeaderboardEntryResponse entry = mapper.toLeaderboardEntryResponse(clan, 4, 15);

        assertAll("Verify LeaderboardEntryResponse from clan",
                () -> assertEquals(CLAN_ID, entry.clanId(), MSG_ID),
                () -> assertEquals(CLAN_NAME, entry.clanName(), MSG_NAME),
                () -> assertEquals(Tier.DIAMOND.getDisplayName(), entry.tier(), MSG_TIER),
                () -> assertEquals(999, entry.score(), MSG_SCORE),
                () -> assertEquals(4, entry.rank(), MSG_RANK),
                () -> assertEquals(15, entry.memberCount(), MSG_MEMBER_COUNT)
        );
    }

    @Test
    void toSeasonStatusResponse_ShouldMapCorrectly() {
        SeasonState state = new SeasonState();
        state.setSeasonNumber(4);
        state.setActive(true);

        SeasonStatusResponse response = mapper.toSeasonStatusResponse(state);

        assertAll("Verify SeasonStatusResponse from state",
                () -> assertEquals(4, response.seasonNumber(), MSG_SEASON_NUM),
                () -> assertEquals("Active", response.status(), MSG_SEASON_STATUS)
        );
    }

    @Test
    void toSeasonStatusResponse_WhenInactive_ShouldMapStatusToEnded() {
        SeasonState state = new SeasonState();
        state.setSeasonNumber(4);
        state.setActive(false);

        SeasonStatusResponse response = mapper.toSeasonStatusResponse(state);

        assertAll("Verify SeasonStatusResponse from inactive state",
                () -> assertEquals("Ended", response.status(), MSG_SEASON_STATUS)
        );
    }

    @Test
    void toDefaultSeasonStatusResponse_ShouldMapToDefault() {
        SeasonStatusResponse response = mapper.toDefaultSeasonStatusResponse();

        assertAll("Verify default SeasonStatusResponse",
                () -> assertEquals(1, response.seasonNumber(), MSG_SEASON_NUM),
                () -> assertEquals("Active", response.status(), MSG_SEASON_STATUS)
        );
    }

    @Test
    void toSeasonClanSummary_ShouldMapCorrectly() {
        Clan clan = new Clan();
        clan.setId(CLAN_ID);
        clan.setName(CLAN_NAME);
        clan.setTier(Tier.GOLD);
        clan.setScore(600);

        SeasonClanSummary summary = mapper.toSeasonClanSummary(clan, 8);

        assertAll("Verify SeasonClanSummary mapping",
                () -> assertEquals(CLAN_ID, summary.clanId(), MSG_ID),
                () -> assertEquals(CLAN_NAME, summary.clanName(), MSG_NAME),
                () -> assertEquals(Tier.GOLD.getDisplayName(), summary.tier(), MSG_TIER),
                () -> assertEquals(600, summary.score(), MSG_SCORE),
                () -> assertEquals(8, summary.memberCount(), MSG_MEMBER_COUNT)
        );
    }

    @Test
    void toSeasonEndResponse_ShouldMapCorrectly() {
        SeasonClanSummary promoted = new SeasonClanSummary(CLAN_ID, CLAN_NAME, Tier.GOLD.getDisplayName(), 600, 8);
        SeasonTierSummary tierSummary = new SeasonTierSummary(Tier.BRONZE.getDisplayName(), List.of(promoted));

        SeasonEndResponse response = mapper.toSeasonEndResponse(
                3, 4, List.of(promoted), List.of(), List.of(), List.of(tierSummary));

        assertAll("Verify SeasonEndResponse mapping",
                () -> assertEquals(3, response.processedSeasonNumber(), "Processed season number should match"),
                () -> assertEquals(4, response.newSeasonNumber(), "New season number should match"),
                () -> assertEquals(1, response.promotedClans().size(), "Promoted list size should match"),
                () -> assertEquals(0, response.relegatedClans().size(), "Relegated list size should match"),
                () -> assertEquals(0, response.unchangedClans().size(), "Unchanged list size should match"),
                () -> assertEquals(1, response.tierSummaries().size(), "Tier summaries list size should match")
        );
    }
}
