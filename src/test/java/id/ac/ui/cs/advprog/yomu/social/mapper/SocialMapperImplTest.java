package id.ac.ui.cs.advprog.yomu.social.mapper;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.dto.*;
import id.ac.ui.cs.advprog.yomu.social.model.*;

class SocialMapperImplTest {

    private static final String CLAN_1 = "clan-1";
    private static final String CLAN_NAME = "Name";
    private static final String CLAN_DESC = "Desc";
    private static final String LEADER_USER = "leader";
    private static final String ACTIVE_STATUS = "Active";

    private static final String MSG_CLAN_ID = "Clan ID should match";
    private static final String MSG_CLAN_NAME = "Clan name should match";
    private static final String MSG_DESCRIPTION = "Description should match";
    private static final String MSG_TIER = "Tier should match";
    private static final String MSG_SCORE = "Score should match";
    private static final String MSG_MEMBER_COUNT = "Member count should match";
    private static final String MSG_RANK = "Rank should match";

    private SocialMapperImpl mapper;

    @BeforeEach
    void setUp() {
        mapper = new SocialMapperImpl();
    }

    @Test
    void testToClanSummaryResponse() {
        ClanSummaryRow row = mock(ClanSummaryRow.class);
        when(row.getClanId()).thenReturn(CLAN_1);
        when(row.getClanName()).thenReturn(CLAN_NAME);
        when(row.getDescription()).thenReturn(CLAN_DESC);
        when(row.getLeaderUsername()).thenReturn(LEADER_USER);
        when(row.getTier()).thenReturn(Tier.BRONZE);
        when(row.getScore()).thenReturn(100);
        when(row.getMemberCount()).thenReturn(5L);

        List<ClanModifierDTO> buffs = List.of(new ClanModifierDTO("Buff", "x1.2", "buff", ACTIVE_STATUS, CLAN_DESC));
        List<ClanModifierDTO> debuffs = List.of();

        ClanSummaryResponse result = mapper.toClanSummaryResponse(row, buffs, debuffs, 120);

        assertAll("Verify ClanSummaryResponse mapping",
                () -> assertEquals(CLAN_1, result.id(), MSG_CLAN_ID),
                () -> assertEquals(CLAN_NAME, result.name(), MSG_CLAN_NAME),
                () -> assertEquals(CLAN_DESC, result.description(), MSG_DESCRIPTION),
                () -> assertEquals(LEADER_USER, result.leaderUsername(), "Leader username should match"),
                () -> assertEquals("BRONZE", result.tier(), MSG_TIER),
                () -> assertEquals(100, result.score(), MSG_SCORE),
                () -> assertEquals(120, result.effectiveScore(), "Effective score should match"),
                () -> assertEquals(5L, result.memberCount(), MSG_MEMBER_COUNT),
                () -> assertEquals(buffs, result.activeBuffs(), "Active buffs should match"),
                () -> assertEquals(debuffs, result.debuffs(), "Debuffs should match"));
    }

    @Test
    void testToClanDetailResponse() {
        Clan clan = new Clan();
        clan.setId(CLAN_1);
        clan.setName(CLAN_NAME);
        clan.setDescription(null); // test null description handling
        clan.setLeaderUsername(LEADER_USER);
        clan.setTier(Tier.GOLD);
        clan.setScore(500);

        List<ClanMemberDTO> memberDTOs = List.of(new ClanMemberDTO("user", "MEMBER", 0, 0, true));
        List<ClanModifierDTO> buffs = List.of();
        List<ClanModifierDTO> debuffs = List.of();

        ClanDetailResponse result = mapper.toClanDetailResponse(clan, 3, 1, memberDTOs, buffs, debuffs);

        // Test non-null description mutation
        clan.setDescription("Hello");
        ClanDetailResponse result2 = mapper.toClanDetailResponse(clan, 3, 1, memberDTOs, buffs, debuffs);

        assertAll("Verify ClanDetailResponse mapping",
                () -> assertEquals(CLAN_1, result.id(), MSG_CLAN_ID),
                () -> assertEquals(CLAN_NAME, result.name(), MSG_CLAN_NAME),
                () -> assertEquals("", result.description(), "Null description should default to empty string"),
                () -> assertEquals(LEADER_USER, result.leaderUsername(), "Leader username should match"),
                () -> assertEquals("GOLD", result.tier(), MSG_TIER),
                () -> assertEquals(3, result.rank(), MSG_RANK),
                () -> assertEquals(500, result.score(), MSG_SCORE),
                () -> assertEquals(1, result.memberCount(), MSG_MEMBER_COUNT),
                () -> assertEquals(SocialConstants.MAX_CLAN_SIZE, result.maxMembers(), "Max members count should match constant"),
                () -> assertEquals(0.0, result.avgAccuracy(), "Average accuracy should match"),
                () -> assertEquals(memberDTOs, result.members(), "Members list should match"),
                () -> assertEquals("Hello", result2.description(), "Non-null description should match input"));
    }

    @Test
    void testToMyClanResponse() {
        Clan clan = new Clan();
        clan.setId(CLAN_1);
        clan.setName(CLAN_NAME);
        clan.setDescription(CLAN_DESC);
        clan.setLeaderUsername(LEADER_USER);
        clan.setTier(Tier.DIAMOND);
        clan.setScore(1000);

        ClanMember member = new ClanMember();
        member.setUsername("user");

        MyClanResponse result = mapper.toMyClanResponse(clan, "LEADER", 1, List.of(member));

        assertAll("Verify MyClanResponse mapping",
                () -> assertEquals(CLAN_1, result.id(), MSG_CLAN_ID),
                () -> assertEquals(CLAN_NAME, result.name(), MSG_CLAN_NAME),
                () -> assertEquals(CLAN_DESC, result.description(), MSG_DESCRIPTION),
                () -> assertEquals(LEADER_USER, result.leaderUsername(), "Leader username should match"),
                () -> assertEquals("LEADER", result.role(), "Role should match"),
                () -> assertEquals("Diamond", result.tier(), MSG_TIER),
                () -> assertEquals(1000, result.score(), MSG_SCORE),
                () -> assertEquals(1, result.rank(), MSG_RANK),
                () -> assertEquals(1, result.members().size(), "Members count should match"));
    }

    @Test
    void testToClanMemberDTO() {
        ClanMember member = new ClanMember();
        member.setUsername("user1");
        member.setRole(ClanRole.LEADER);

        ClanMemberDTO result = mapper.toClanMemberDTO(member);

        assertAll("Verify ClanMemberDTO mapping",
                () -> assertEquals("user1", result.username(), "Username should match"),
                () -> assertEquals("LEADER", result.role(), "Role should match"),
                () -> assertEquals(0, result.contribution(), "Contribution should default to 0"),
                () -> assertEquals(0, result.streak(), "Streak should default to 0"),
                () -> assertTrue(result.isOnline(), "isOnline should default to true"));
    }

    @Test
    void testToClanModifierDTO_DailyMissionBuff() {
        ClanModifier modifier = new ClanModifier();
        modifier.setKey(SocialConstants.DAILY_MISSION_BUFF_KEY);
        modifier.setMultiplier(1.2);
        modifier.setType(ModifierType.BUFF);
        modifier.setEndAt(null);

        ClanModifierDTO result = mapper.toClanModifierDTO(modifier);

        assertAll("Verify daily mission buff modifier mapping",
                () -> assertEquals("Daily Mission Buff", result.name(), "Modifier name should match"),
                () -> assertEquals("x1.20", result.multiplier(), "Formatted multiplier should match"),
                () -> assertEquals("buff", result.type(), "Modifier type should be buff"),
                () -> assertEquals(ACTIVE_STATUS, result.duration(), "Duration should be Active"),
                () -> assertEquals("+20% Points Multiplier", result.description(), "Description should match"));
    }

    @Test
    void testToClanModifierDTO_LowAccuracyPenalty() {
        Instant end = Instant.parse("2026-05-22T12:00:00Z");
        ClanModifier modifier = new ClanModifier();
        modifier.setKey(SocialConstants.LOW_ACCURACY_PENALTY_KEY);
        modifier.setMultiplier(0.8);
        modifier.setType(ModifierType.DEBUFF);
        modifier.setEndAt(end);

        ClanModifierDTO result = mapper.toClanModifierDTO(modifier);

        assertAll("Verify penalty modifier mapping",
                () -> assertEquals("Low Accuracy Penalty", result.name(), "Modifier name should match"),
                () -> assertEquals("x0.80", result.multiplier(), "Formatted multiplier should match"),
                () -> assertEquals("debuff", result.type(), "Modifier type should be debuff"),
                () -> assertEquals("Until " + end.toString(), result.duration(), "Duration format should match end timestamp"),
                () -> assertEquals("-20% Points Multiplier", result.description(), "Description should match"));
    }

    @Test
    void testToClanModifierDTO_OtherKey() {
        ClanModifier modifier = new ClanModifier();
        modifier.setKey("CustomKey");
        modifier.setMultiplier(1.5);
        modifier.setType(ModifierType.BUFF);

        ClanModifierDTO result = mapper.toClanModifierDTO(modifier);

        assertAll("Verify custom modifier mapping",
                () -> assertEquals("CustomKey", result.name(), "Modifier name should match key"),
                () -> assertEquals("BUFF modifier", result.description(), "Description should match fall-through style"));
    }

    @Test
    void testToLeaderboardEntryResponse_FromRow() {
        ClanLeaderboardRow row = mock(ClanLeaderboardRow.class);
        when(row.getClanId()).thenReturn(CLAN_1);
        when(row.getClanName()).thenReturn(CLAN_NAME);
        when(row.getTier()).thenReturn(Tier.SILVER);
        when(row.getScore()).thenReturn(200);
        when(row.getMemberCount()).thenReturn(10L);

        LeaderboardEntryResponse result = mapper.toLeaderboardEntryResponse(row, 2);

        assertAll("Verify LeaderboardEntryResponse from row mapping",
                () -> assertEquals(CLAN_1, result.clanId(), MSG_CLAN_ID),
                () -> assertEquals(CLAN_NAME, result.clanName(), MSG_CLAN_NAME),
                () -> assertEquals("Silver", result.tier(), MSG_TIER),
                () -> assertEquals(200, result.score(), MSG_SCORE),
                () -> assertEquals(2, result.rank(), MSG_RANK),
                () -> assertEquals(10, result.memberCount(), MSG_MEMBER_COUNT));
    }

    @Test
    void testToLeaderboardEntryResponse_FromClan() {
        Clan clan = new Clan();
        clan.setId(CLAN_1);
        clan.setName(CLAN_NAME);
        clan.setTier(Tier.SILVER);
        clan.setScore(200);

        LeaderboardEntryResponse result = mapper.toLeaderboardEntryResponse(clan, 2, 10);

        assertAll("Verify LeaderboardEntryResponse from clan mapping",
                () -> assertEquals(CLAN_1, result.clanId(), MSG_CLAN_ID),
                () -> assertEquals(CLAN_NAME, result.clanName(), MSG_CLAN_NAME),
                () -> assertEquals("Silver", result.tier(), MSG_TIER),
                () -> assertEquals(200, result.score(), MSG_SCORE),
                () -> assertEquals(2, result.rank(), MSG_RANK),
                () -> assertEquals(10, result.memberCount(), MSG_MEMBER_COUNT));
    }

    @Test
    void testSeasonMappings() {
        SeasonState state = new SeasonState();
        state.setSeasonNumber(5);
        state.setActive(true);

        SeasonStatusResponse status1 = mapper.toSeasonStatusResponse(state);
        state.setActive(false);
        SeasonStatusResponse status2 = mapper.toSeasonStatusResponse(state);
        SeasonStatusResponse statusDefault = mapper.toDefaultSeasonStatusResponse();

        Clan clan = new Clan();
        clan.setId(CLAN_1);
        clan.setName(CLAN_NAME);
        clan.setTier(Tier.DIAMOND);
        clan.setScore(100);

        SeasonClanSummary summary = mapper.toSeasonClanSummary(clan, 5);
        SeasonEndResponse endResponse = mapper.toSeasonEndResponse(5, 6, List.of(summary), List.of(), List.of(), List.of());

        assertAll("Verify all season mapping operations",
                () -> assertEquals(5, status1.seasonNumber(), "Season number 1 should match"),
                () -> assertEquals(ACTIVE_STATUS, status1.status(), "Status 1 should match"),
                () -> assertEquals("Ended", status2.status(), "Status 2 should match"),
                () -> assertEquals(1, statusDefault.seasonNumber(), "Default season number should match"),
                () -> assertEquals(ACTIVE_STATUS, statusDefault.status(), "Default status should match"),
                () -> assertEquals(CLAN_1, summary.clanId(), "Summary clan ID should match"),
                () -> assertEquals(CLAN_NAME, summary.clanName(), "Summary clan name should match"),
                () -> assertEquals("Diamond", summary.tier(), "Summary tier should match"),
                () -> assertEquals(100, summary.score(), "Summary score should match"),
                () -> assertEquals(5, summary.memberCount(), "Summary member count should match"),
                () -> assertEquals(5, endResponse.processedSeasonNumber(), "Processed season number should match"),
                () -> assertEquals(6, endResponse.newSeasonNumber(), "New season number should match"),
                () -> assertEquals(1, endResponse.promotedClans().size(), "Promoted clans list size should match"));
    }
}
