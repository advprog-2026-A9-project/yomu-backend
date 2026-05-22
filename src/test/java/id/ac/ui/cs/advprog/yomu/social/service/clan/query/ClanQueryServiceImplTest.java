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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
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
@SuppressWarnings("null")
class ClanQueryServiceImplTest {

    private static final String CLAN_123 = "clan-123";
    private static final String WIBU_ELITE = "Wibu Elite";
    private static final String LEADER_1 = "leader-1";
    private static final String BRONZE_TIER = "Bronze";
    private static final String SEARCH_QUERY = "search";
    private static final String USER_1 = "user-1";

    @Mock
    private ClanRepository clanRepository;

    @Mock
    private ClanMemberRepository memberRepository;

    @Mock
    private ClanValidator clanValidator;

    @Mock
    private SocialMapper socialMapper;

    @Mock
    private ClanModifierService modifierService;

    private ClanQueryServiceImpl queryService;

    private Clan dummyClan;
    private ClanSummaryRow mockSummaryRow;
    private ClanSummaryResponse mockSummaryResponse;

    @BeforeEach
    void setUp() {
        queryService = new ClanQueryServiceImpl(clanRepository, memberRepository, clanValidator, socialMapper, modifierService);

        dummyClan = new Clan();
        dummyClan.setId(CLAN_123);
        dummyClan.setName(WIBU_ELITE);
        dummyClan.setLeaderUsername(LEADER_1);
        dummyClan.setTier(Tier.BRONZE);
        dummyClan.setScore(100);

        mockSummaryRow = mock(ClanSummaryRow.class);
        lenient().when(mockSummaryRow.getClanId()).thenReturn(CLAN_123);
        lenient().when(mockSummaryRow.getScore()).thenReturn(100);

        mockSummaryResponse = new ClanSummaryResponse(CLAN_123, WIBU_ELITE, "Descr", LEADER_1, BRONZE_TIER, 100, 100, 1L, List.of(), List.of());
    }

    @Test
    void findAll_WhenSearchEmpty_ShouldQueryAll() {
        when(clanRepository.findAllClanSummaries(any(PageRequest.class))).thenReturn(List.of(mockSummaryRow));
        when(modifierService.getModifierSummaries(anyList())).thenReturn(Map.of(CLAN_123, new ModifierSummary(List.of(), List.of(), 1.2)));
        when(socialMapper.toClanSummaryResponse(eq(mockSummaryRow), anyList(), anyList(), eq(120)))
                .thenReturn(mockSummaryResponse);

        List<ClanSummaryResponse> result = queryService.findAll("");

        assertAll("Verify find all search results",
                () -> assertFalse(result.isEmpty(), "Results list should not be empty"),
                () -> verify(clanRepository).findAllClanSummaries(any(PageRequest.class)));
    }

    @Test
    void findAll_WhenSearchNotEmpty_ShouldQueryByQuery() {
        when(clanRepository.findClanSummariesByQuery(eq(SEARCH_QUERY), any(PageRequest.class))).thenReturn(List.of(mockSummaryRow));
        when(modifierService.getModifierSummaries(anyList())).thenReturn(Map.of());
        when(socialMapper.toClanSummaryResponse(eq(mockSummaryRow), anyList(), anyList(), eq(100)))
                .thenReturn(mockSummaryResponse);

        List<ClanSummaryResponse> result = queryService.findAll(SEARCH_QUERY);

        assertAll("Verify find all search results with query",
                () -> assertFalse(result.isEmpty(), "Results list should not be empty"),
                () -> verify(clanRepository).findClanSummariesByQuery(eq(SEARCH_QUERY), any(PageRequest.class)));
    }

    @Test
    void findRandomClans_WhenLimitZeroOrNegative_ShouldReturnEmpty() {
        assertAll("Verify random clans empty cases",
                () -> assertTrue(queryService.findRandomClans(0).isEmpty(), "Zero limit should yield empty list"),
                () -> assertTrue(queryService.findRandomClans(-1).isEmpty(), "Negative limit should yield empty list"));
    }

    @Test
    void findRandomClans_WhenAvailableIdsLessThanLimit_ShouldWrapAroundAndMerge() {
        when(clanRepository.findRandomIds(anyString(), eq(PageRequest.of(0, 3)))).thenReturn(List.of("id-1"));
        when(clanRepository.findRandomIds(eq("0"), eq(PageRequest.of(0, 2)))).thenReturn(List.of("id-1", "id-2"));
        when(clanRepository.findClanSummariesByIds(anyList())).thenReturn(List.of(mockSummaryRow));
        when(modifierService.getModifierSummaries(anyList())).thenReturn(Map.of());
        when(socialMapper.toClanSummaryResponse(eq(mockSummaryRow), anyList(), anyList(), eq(100)))
                .thenReturn(mockSummaryResponse);

        List<ClanSummaryResponse> result = queryService.findRandomClans(3);

        assertAll("Verify wrap around random clans result",
                () -> assertFalse(result.isEmpty(), "Random clans result should not be empty"));
    }

    @Test
    void findRandomClans_WhenNoIdsFound_ShouldReturnEmpty() {
        when(clanRepository.findRandomIds(anyString(), any(PageRequest.class))).thenReturn(List.of());
        when(clanRepository.findRandomIds(eq("0"), any(PageRequest.class))).thenReturn(List.of());

        List<ClanSummaryResponse> result = queryService.findRandomClans(3);

        assertAll("Verify no random clans found behavior",
                () -> assertTrue(result.isEmpty(), "Random clans result should be empty"));
    }

    @Test
    void getClanDetail_WhenClanNotFound_ShouldThrowException() {
        when(clanRepository.findById(CLAN_123)).thenReturn(Optional.empty());

        assertAll("Verify get clan detail not found exception",
                () -> {
                    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                            () -> queryService.getClanDetail(CLAN_123),
                            "Should throw IllegalArgumentException");
                    assertEquals(SocialConstants.CLAN_NOT_FOUND_MESSAGE, ex.getMessage(), "Message should match");
                });
    }

    @Test
    void getClanDetail_WhenValid_ShouldReturnDetail() {
        ClanMember member = new ClanMember();
        member.setUsername(USER_1);
        ClanMemberDTO memberDTO = new ClanMemberDTO(USER_1, "MEMBER", 100, 5, true);

        when(clanRepository.findById(CLAN_123)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.getClanMembersByClanId(CLAN_123)).thenReturn(List.of(member));
        when(socialMapper.toClanMemberDTO(member)).thenReturn(memberDTO);
        when(clanRepository.findRankByTierAndScore(Tier.BRONZE, 100, CLAN_123)).thenReturn(1L);
        when(modifierService.getModifierSummary(CLAN_123)).thenReturn(new ModifierSummary(List.of(), List.of(), 1.0));

        ClanDetailResponse expected = new ClanDetailResponse(CLAN_123, WIBU_ELITE, "Descr", LEADER_1, BRONZE_TIER, 1, 100, 1, 50, 95.0, List.of(memberDTO), List.of(), List.of());
        when(socialMapper.toClanDetailResponse(eq(dummyClan), eq(1), eq(1), anyList(), anyList(), anyList()))
                .thenReturn(expected);

        ClanDetailResponse result = queryService.getClanDetail(CLAN_123);

        assertAll("Verify clan detail retrieval details",
                () -> assertNotNull(result, "Resulting detail should not be null"),
                () -> assertEquals(CLAN_123, result.id(), "Clan ID should match"),
                () -> assertEquals(1, result.rank(), "Clan rank should match"),
                () -> verify(clanValidator).requireClanId(CLAN_123));
    }

    @Test
    void getMyClanByUsername_WhenNoMembership_ShouldReturnEmpty() {
        when(memberRepository.findByUsername(USER_1)).thenReturn(Optional.empty());

        Optional<MyClanResponse> result = queryService.getMyClanByUsername(USER_1);

        assertAll("Verify get my clan with no membership",
                () -> assertTrue(result.isEmpty(), "Should return empty Optional"));
    }

    @Test
    void getMyClanByUsername_WhenHasMembership_ShouldReturnMyClanResponse() {
        ClanMember membership = new ClanMember();
        membership.setClanId(CLAN_123);
        membership.setUsername(USER_1);

        when(memberRepository.findByUsername(USER_1)).thenReturn(Optional.of(membership));
        when(clanRepository.findById(CLAN_123)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.getClanMembersByClanId(CLAN_123)).thenReturn(List.of(membership));
        when(clanRepository.findRankByTierAndScore(Tier.BRONZE, 100, CLAN_123)).thenReturn(2L);

        MyClanResponse expected = new MyClanResponse(CLAN_123, WIBU_ELITE, "Descr", LEADER_1, "ANGGOTA", BRONZE_TIER, 100, 2, List.of(membership));
        when(socialMapper.toMyClanResponse(eq(dummyClan), eq("ANGGOTA"), eq(2), anyList())).thenReturn(expected);

        Optional<MyClanResponse> result = queryService.getMyClanByUsername(USER_1);

        assertAll("Verify user's clan membership details",
                () -> assertTrue(result.isPresent(), "MyClanResponse should be present"),
                () -> assertEquals("ANGGOTA", result.get().role(), "Role should match"),
                () -> assertEquals(2, result.get().rank(), "Rank should match"));
    }

    @Test
    void getLeaderboardByTier_ShouldBuildLeaderboardForEveryTier() {
        when(clanRepository.findLeaderboardByTier(any(Tier.class), any(PageRequest.class))).thenReturn(List.of());

        List<LeaderboardResponse> result = queryService.getLeaderboardByTier(null, null);

        assertAll("Verify leaderboard list matches tier size",
                () -> assertEquals(Tier.values().length, result.size(), "Resulting list size should equal the number of tiers"));
    }

    @Test
    void getLeaderboardByTier_WhenUserHasClanInTier_ShouldAddUserEntry() {
        ClanMember membership = new ClanMember();
        membership.setClanId(CLAN_123);
        membership.setUsername(LEADER_1);

        ClanLeaderboardRow row = mock(ClanLeaderboardRow.class);
        when(clanRepository.findLeaderboardByTierAndName(eq(Tier.BRONZE), eq(SEARCH_QUERY), any(PageRequest.class)))
                .thenReturn(List.of(row));

        LeaderboardEntryResponse rowResponse = new LeaderboardEntryResponse(CLAN_123, WIBU_ELITE, "Bronze", 100, 1, 1);
        when(socialMapper.toLeaderboardEntryResponse(eq(row), eq(1))).thenReturn(rowResponse);

        when(memberRepository.findByUsername(LEADER_1)).thenReturn(Optional.of(membership));
        when(clanRepository.findById(CLAN_123)).thenReturn(Optional.of(dummyClan));

        when(clanRepository.findRankByTierAndScore(Tier.BRONZE, 100, CLAN_123)).thenReturn(1L);
        when(memberRepository.countByClanId(CLAN_123)).thenReturn(1L);
        when(socialMapper.toLeaderboardEntryResponse(eq(dummyClan), eq(1), eq(1))).thenReturn(rowResponse);

        List<LeaderboardResponse> result = queryService.getLeaderboardByTier(LEADER_1, SEARCH_QUERY);

        // The user entry for BRONZE leaderboard should be non-null
        LeaderboardResponse bronzeLeaderboard = result.stream()
                .filter(l -> "BRONZE".equalsIgnoreCase(l.tier()))
                .findFirst()
                .orElseThrow();

        assertAll("Verify leaderboard results",
                () -> assertNotNull(bronzeLeaderboard.userEntry(), "Bronze user entry should not be null"),
                () -> assertEquals(CLAN_123, bronzeLeaderboard.userEntry().clanId(), "User entry clan ID should match"));
    }
}
