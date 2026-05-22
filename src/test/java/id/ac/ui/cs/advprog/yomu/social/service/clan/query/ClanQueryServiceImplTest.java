package id.ac.ui.cs.advprog.yomu.social.service.clan.query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanDetailResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanLeaderboardRow;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanMemberDTO;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryRow;
import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardEntryResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.ModifierSummary;
import id.ac.ui.cs.advprog.yomu.social.dto.MyClanResponse;
import id.ac.ui.cs.advprog.yomu.social.mapper.SocialMapper;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanRepository;
import id.ac.ui.cs.advprog.yomu.social.service.modifier.ClanModifierService;
import id.ac.ui.cs.advprog.yomu.social.validation.ClanValidator;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"null", "PMD"})
class ClanQueryServiceImplTest {

    // ── Shared data constants ─────────────────────────────────────────────────
    private static final String CLAN_ID      = "clan-1";
    private static final String CLAN_NAME    = "Wibu Elite";
    private static final String CLAN_DESC    = "Deskripsi";
    private static final String LEADER       = "leader-1";
    private static final String USER_1       = "user-1";
    private static final String SEARCH_QUERY = "wibu";
    private static final String TIER_BRONZE  = "BRONZE";

    // ── Assertion message constants ───────────────────────────────────────────
    private static final String MSG_NOT_NULL    = "Result should not be null";
    private static final String MSG_NOT_EMPTY   = "Result list should not be empty";
    private static final String MSG_SIZE        = "Result list size should match";
    private static final String MSG_ID          = "Clan ID should match";
    private static final String MSG_RANK        = "Rank should match";
    private static final String MSG_ROLE        = "Role should match";
    private static final String MSG_TIER_COUNT  = "Leaderboard should have one entry per tier";
    private static final String MSG_THROW       = "Should throw IllegalArgumentException when clan not found";
    private static final String MSG_EMPTY_OPT   = "getMyClan should return empty when user has no clan";

    @Mock private ClanRepository clanRepository;
    @Mock private ClanMemberRepository memberRepository;
    @Mock private ClanValidator clanValidator;
    @Mock private SocialMapper socialMapper;
    @Mock private ClanModifierService modifierService;

    @InjectMocks
    private ClanQueryServiceImpl queryService;

    private Clan dummyClan;
    private ClanMember dummyMember;
    private ClanSummaryRow mockSummaryRow;
    private ClanSummaryResponse mockSummaryResponse;
    private ClanDetailResponse mockDetailResponse;
    private ModifierSummary noModifier;

    @BeforeEach
    void setUp() {
        dummyClan = new Clan();
        dummyClan.setId(CLAN_ID);
        dummyClan.setName(CLAN_NAME);
        dummyClan.setDescription(CLAN_DESC);
        dummyClan.setLeaderUsername(LEADER);
        dummyClan.setTier(Tier.BRONZE);
        dummyClan.setScore(100);

        dummyMember = new ClanMember();
        dummyMember.setClanId(CLAN_ID);
        dummyMember.setUsername(USER_1);

        mockSummaryRow = new ClanSummaryRow() {
            public String getClanId()        { return CLAN_ID; }
            public String getClanName()      { return CLAN_NAME; }
            public String getDescription()   { return CLAN_DESC; }
            public String getLeaderUsername(){ return LEADER; }
            public Tier getTier()            { return Tier.BRONZE; }
            public int getScore()            { return 100; }
            public long getMemberCount()     { return 1L; }
        };

        noModifier = new ModifierSummary(List.of(), List.of(), 1.0);

        mockSummaryResponse = new ClanSummaryResponse(
                CLAN_ID, CLAN_NAME, CLAN_DESC, LEADER, TIER_BRONZE, 100, 100, 1, List.of(), List.of());

        mockDetailResponse = new ClanDetailResponse(
                CLAN_ID, CLAN_NAME, CLAN_DESC, LEADER, TIER_BRONZE, 1, 100, 1, 50, 95.0, List.of(), List.of(), List.of());
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Test
    void findAll_WhenSearchBlank_ShouldQueryAll() {
        when(clanRepository.findAllClanSummaries(any(PageRequest.class)))
                .thenReturn(List.of(mockSummaryRow));
        when(modifierService.getModifierSummaries(anyList())).thenReturn(Map.of(CLAN_ID, noModifier));
        when(socialMapper.toClanSummaryResponse(eq(mockSummaryRow), anyList(), anyList(), anyInt()))
                .thenReturn(mockSummaryResponse);

        List<ClanSummaryResponse> result = queryService.findAll("");

        assertAll("Verify findAll with blank search returns all clans",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertFalse(result.isEmpty(), MSG_NOT_EMPTY),
                () -> verify(clanRepository).findAllClanSummaries(any(PageRequest.class)));
    }

    @Test
    void findAll_WhenSearchNull_ShouldQueryAll() {
        when(clanRepository.findAllClanSummaries(any(PageRequest.class)))
                .thenReturn(List.of(mockSummaryRow));
        when(modifierService.getModifierSummaries(anyList())).thenReturn(Map.of(CLAN_ID, noModifier));
        when(socialMapper.toClanSummaryResponse(eq(mockSummaryRow), anyList(), anyList(), anyInt()))
                .thenReturn(mockSummaryResponse);

        List<ClanSummaryResponse> result = queryService.findAll(null);

        assertAll("Verify findAll with null search falls back to query-all",
                () -> assertFalse(result.isEmpty(), MSG_NOT_EMPTY));
    }

    @Test
    void findAll_WhenSearchNotBlank_ShouldQueryBySearch() {
        when(clanRepository.findClanSummariesByQuery(eq(SEARCH_QUERY), any(PageRequest.class)))
                .thenReturn(List.of(mockSummaryRow));
        when(modifierService.getModifierSummaries(anyList())).thenReturn(Map.of(CLAN_ID, noModifier));
        when(socialMapper.toClanSummaryResponse(eq(mockSummaryRow), anyList(), anyList(), anyInt()))
                .thenReturn(mockSummaryResponse);

        List<ClanSummaryResponse> result = queryService.findAll(SEARCH_QUERY);

        assertAll("Verify findAll with search term queries by name",
                () -> assertFalse(result.isEmpty(), MSG_NOT_EMPTY),
                () -> verify(clanRepository).findClanSummariesByQuery(eq(SEARCH_QUERY), any(PageRequest.class)));
    }

    @Test
    void findAll_WhenNoClanExists_ShouldReturnEmptyList() {
        when(clanRepository.findAllClanSummaries(any(PageRequest.class))).thenReturn(List.of());
        when(modifierService.getModifierSummaries(anyList())).thenReturn(Map.of());

        List<ClanSummaryResponse> result = queryService.findAll("");

        assertAll("Verify findAll returns empty list when no clans exist",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertEquals(0, result.size(), "Size should be 0"));
    }

    // ─── findRandomClans ─────────────────────────────────────────────────────

    @Test
    void findRandomClans_WhenLimitZero_ShouldReturnEmpty() {
        List<ClanSummaryResponse> result = queryService.findRandomClans(0);

        assertAll("Verify limit 0 returns empty list",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertEquals(0, result.size(), MSG_SIZE));
    }

    @Test
    void findRandomClans_WhenLimitNegative_ShouldReturnEmpty() {
        List<ClanSummaryResponse> result = queryService.findRandomClans(-5);

        assertAll("Verify negative limit returns empty list",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertEquals(0, result.size(), MSG_SIZE));
    }

    @Test
    void findRandomClans_WhenNoIdsFound_ShouldReturnEmpty() {
        when(clanRepository.findRandomIds(anyString(), any(PageRequest.class))).thenReturn(List.of());

        List<ClanSummaryResponse> result = queryService.findRandomClans(3);

        assertAll("Verify no ids found returns empty list",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertEquals(0, result.size(), MSG_SIZE));
    }

    @Test
    void findRandomClans_WhenIdsFound_ShouldReturnSummaries() {
        when(clanRepository.findRandomIds(anyString(), any(PageRequest.class))).thenReturn(List.of(CLAN_ID));
        when(clanRepository.findClanSummariesByIds(anyList())).thenReturn(List.of(mockSummaryRow));
        when(modifierService.getModifierSummaries(anyList())).thenReturn(Map.of(CLAN_ID, noModifier));
        when(socialMapper.toClanSummaryResponse(eq(mockSummaryRow), anyList(), anyList(), anyInt()))
                .thenReturn(mockSummaryResponse);

        List<ClanSummaryResponse> result = queryService.findRandomClans(1);

        assertAll("Verify random clans are returned",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertFalse(result.isEmpty(), MSG_NOT_EMPTY));
    }

    // ─── getClanDetail ────────────────────────────────────────────────────────

    @Test
    void getClanDetail_WhenClanExists_ShouldReturnDetail() {
        ClanMemberDTO memberDTO = new ClanMemberDTO(USER_1, "MEMBER", 100, 5, true);
        when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.getClanMembersByClanId(CLAN_ID)).thenReturn(List.of(dummyMember));
        when(socialMapper.toClanMemberDTO(dummyMember)).thenReturn(memberDTO);
        when(clanRepository.findRankByTierAndScore(Tier.BRONZE, 100, CLAN_ID)).thenReturn(1L);
        when(modifierService.getModifierSummary(CLAN_ID)).thenReturn(noModifier);
        when(socialMapper.toClanDetailResponse(eq(dummyClan), eq(1), eq(1), anyList(), anyList(), anyList()))
                .thenReturn(mockDetailResponse);

        ClanDetailResponse result = queryService.getClanDetail(CLAN_ID);

        assertAll("Verify clan detail is returned correctly",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertEquals(CLAN_ID, result.id(), MSG_ID),
                () -> assertEquals(1, result.rank(), MSG_RANK),
                () -> verify(clanValidator).requireClanId(CLAN_ID));
    }

    @Test
    void getClanDetail_WhenClanNotFound_ShouldThrow() {
        when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.empty());

        assertAll("Verify exception when clan not found for detail",
                () -> assertThrows(IllegalArgumentException.class,
                        () -> queryService.getClanDetail(CLAN_ID), MSG_THROW));
    }

    // ─── getMyClanByUsername ──────────────────────────────────────────────────

    @Test
    void getMyClanByUsername_WhenUserHasNoClan_ShouldReturnEmpty() {
        when(memberRepository.findByUsername(USER_1)).thenReturn(Optional.empty());

        Optional<MyClanResponse> result = queryService.getMyClanByUsername(USER_1);

        assertAll("Verify empty optional when user has no clan",
                () -> assertTrue(result.isEmpty(), MSG_EMPTY_OPT));
    }

    @Test
    void getMyClanByUsername_WhenUserInClan_ShouldReturnMyClan() {
        dummyMember.setClanId(CLAN_ID);
        when(memberRepository.findByUsername(USER_1)).thenReturn(Optional.of(dummyMember));
        when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.getClanMembersByClanId(CLAN_ID)).thenReturn(List.of(dummyMember));
        when(clanRepository.findRankByTierAndScore(Tier.BRONZE, 100, CLAN_ID)).thenReturn(2L);
        MyClanResponse myResponse = new MyClanResponse(
                CLAN_ID, CLAN_NAME, CLAN_DESC, LEADER, SocialConstants.MY_CLAN_ROLE_MEMBER,
                TIER_BRONZE, 100, 2, List.of(dummyMember));
        when(socialMapper.toMyClanResponse(eq(dummyClan), anyString(), eq(2), anyList()))
                .thenReturn(myResponse);

        Optional<MyClanResponse> result = queryService.getMyClanByUsername(USER_1);

        assertAll("Verify MyClanResponse is returned for clan member",
                () -> assertTrue(result.isPresent(), "Result should be present"),
                () -> assertEquals(CLAN_ID, result.get().id(), MSG_ID),
                () -> assertEquals(SocialConstants.MY_CLAN_ROLE_MEMBER, result.get().role(), MSG_ROLE));
    }

    @Test
    void getMyClanByUsername_WhenUserIsLeader_ShouldReturnLeaderRole() {
        dummyMember.setClanId(CLAN_ID);
        dummyMember.setUsername(LEADER);
        when(memberRepository.findByUsername(LEADER)).thenReturn(Optional.of(dummyMember));
        when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.getClanMembersByClanId(CLAN_ID)).thenReturn(List.of(dummyMember));
        when(clanRepository.findRankByTierAndScore(Tier.BRONZE, 100, CLAN_ID)).thenReturn(1L);
        MyClanResponse leaderResponse = new MyClanResponse(
                CLAN_ID, CLAN_NAME, CLAN_DESC, LEADER, SocialConstants.MY_CLAN_ROLE_LEADER,
                TIER_BRONZE, 100, 1, List.of(dummyMember));
        when(socialMapper.toMyClanResponse(eq(dummyClan), anyString(), eq(1), anyList()))
                .thenReturn(leaderResponse);

        Optional<MyClanResponse> result = queryService.getMyClanByUsername(LEADER);

        assertAll("Verify leader role is returned for clan leader",
                () -> assertTrue(result.isPresent(), "Leader result should be present"),
                () -> assertEquals(SocialConstants.MY_CLAN_ROLE_LEADER, result.get().role(), MSG_ROLE));
    }

    // ─── getLeaderboardByTier ─────────────────────────────────────────────────

    @Test
    void getLeaderboardByTier_WhenNoUser_ShouldReturnAllTiers() {
        when(clanRepository.findLeaderboardByTier(any(), any(PageRequest.class))).thenReturn(List.of());

        List<LeaderboardResponse> result = queryService.getLeaderboardByTier(null, null);

        assertAll("Verify leaderboard returns an entry for every tier",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertEquals(Tier.values().length, result.size(), MSG_TIER_COUNT));
    }

    @Test
    void getLeaderboardByTier_WhenSearchProvided_ShouldQueryByName() {
        when(clanRepository.findLeaderboardByTierAndName(any(), eq(SEARCH_QUERY), any(PageRequest.class)))
                .thenReturn(List.of());

        List<LeaderboardResponse> result = queryService.getLeaderboardByTier(null, SEARCH_QUERY);

        assertAll("Verify leaderboard uses name filter when search is provided",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> assertEquals(Tier.values().length, result.size(), MSG_TIER_COUNT));
    }

    @Test
    void getLeaderboardByTier_WhenUserHasClan_ShouldIncludeUserEntry() {
        dummyMember.setClanId(CLAN_ID);
        when(memberRepository.findByUsername(USER_1)).thenReturn(Optional.of(dummyMember));
        when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        when(clanRepository.findLeaderboardByTier(any(), any(PageRequest.class))).thenReturn(List.of());
        when(clanRepository.findRankByTierAndScore(eq(Tier.BRONZE), anyInt(), eq(CLAN_ID))).thenReturn(1L);
        when(memberRepository.countByClanId(CLAN_ID)).thenReturn(3L);
        LeaderboardEntryResponse userEntry = new LeaderboardEntryResponse(
                CLAN_ID, CLAN_NAME, TIER_BRONZE, 100, 1, 3);
        when(socialMapper.toLeaderboardEntryResponse(eq(dummyClan), eq(1), eq(3)))
                .thenReturn(userEntry);

        List<LeaderboardResponse> result = queryService.getLeaderboardByTier(USER_1, null);

        assertAll("Verify user entry appears in the BRONZE tier leaderboard",
                () -> assertNotNull(result, MSG_NOT_NULL),
                () -> {
                    LeaderboardResponse bronzeBoard = result.stream()
                            .filter(r -> r.tier().equalsIgnoreCase(Tier.BRONZE.getDisplayName()))
                            .findFirst()
                            .orElse(null);
                    assertNotNull(bronzeBoard, "Bronze tier leaderboard should be present");
                    assertNotNull(bronzeBoard.userEntry(), "User entry should be present in bronze board");
                });
    }
}
