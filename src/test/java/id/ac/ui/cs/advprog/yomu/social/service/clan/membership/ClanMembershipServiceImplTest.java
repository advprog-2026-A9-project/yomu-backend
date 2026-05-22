package id.ac.ui.cs.advprog.yomu.social.service.clan.membership;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import id.ac.ui.cs.advprog.yomu.social.event.ClanShouldBeDeletedEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserJoinClanEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserLeaveClanEvent;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.model.ClanRole;
import id.ac.ui.cs.advprog.yomu.social.model.Tier;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanRepository;
import id.ac.ui.cs.advprog.yomu.social.validation.ClanValidator;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD")
class ClanMembershipServiceImplTest {

    private static final String CLAN_ID = "clan-123";
    private static final String CLAN_NAME = "Wibu Indo";
    private static final String LEADER_USER = "leader-username";
    private static final String MEMBER_USER = "member-username";
    private static final String OTHER_MEMBER_USER = "other-member-username";
    private static final String BRONZE_TIER_STR = "BRONZE";
    private static final String ROLE_MEMBER = "MEMBER";
    private static final String ROLE_LEADER = "LEADER";

    // Assertion Messages
    private static final String MSG_EXCEPTION_MSG = "Exception message should match";
    private static final String MSG_MEMBERS_SIZE = "Members list size should match";

    @Mock
    private ClanRepository clanRepository;

    @Mock
    private ClanMemberRepository memberRepository;

    @Mock
    private ClanValidator clanValidator;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ClanMembershipServiceImpl membershipService;

    private Clan dummyClan;
    private ClanMember leaderMember;
    private ClanMember normalMember;

    @BeforeEach
    void setUp() {
        dummyClan = new Clan();
        dummyClan.setId(CLAN_ID);
        dummyClan.setName(CLAN_NAME);
        dummyClan.setLeaderUsername(LEADER_USER);
        dummyClan.setTier(Tier.BRONZE);

        leaderMember = new ClanMember();
        leaderMember.setClanId(CLAN_ID);
        leaderMember.setUsername(LEADER_USER);
        leaderMember.setRole(ClanRole.LEADER);

        normalMember = new ClanMember();
        normalMember.setClanId(CLAN_ID);
        normalMember.setUsername(MEMBER_USER);
        normalMember.setRole(ClanRole.MEMBER);
    }

    @Test
    void joinClan_Success() {
        when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.findByClanIdAndUsername(CLAN_ID, MEMBER_USER)).thenReturn(Optional.empty());
        when(memberRepository.findByUsername(MEMBER_USER)).thenReturn(Optional.empty());
        when(memberRepository.countByClanId(CLAN_ID)).thenReturn(5L);

        membershipService.joinClan(CLAN_ID, MEMBER_USER, ROLE_MEMBER);

        verify(clanValidator).requireClanId(CLAN_ID);
        verify(clanValidator).requireUsername(MEMBER_USER);
        verify(clanValidator).requireNotAlreadyMember(false);
        verify(clanValidator).requireNotMemberOfOtherClan(false);
        verify(clanValidator).requireClanNotFull(5L);
        verify(memberRepository).save(any(ClanMember.class));
        verify(eventPublisher).publishEvent(any(UserJoinClanEvent.class));
    }

    @Test
    void joinClan_ClanNotFound_ThrowsException() {
        when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            membershipService.joinClan(CLAN_ID, MEMBER_USER, ROLE_MEMBER),
            "Should throw IllegalArgumentException when clan not found"
        );

        assertEquals("Clan tidak ditemukan.", ex.getMessage(), MSG_EXCEPTION_MSG);
        verify(memberRepository, never()).save(any(ClanMember.class));
    }

    @Test
    void leaveClan_NormalMember_Success() {
        when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.findByClanIdAndUsername(CLAN_ID, MEMBER_USER)).thenReturn(Optional.of(normalMember));

        membershipService.leaveClan(CLAN_ID, MEMBER_USER);

        verify(clanValidator).requireClanId(CLAN_ID);
        verify(clanValidator).requireUsername(MEMBER_USER);
        verify(memberRepository).deleteByClanIdAndUsername(CLAN_ID, MEMBER_USER);
        verify(eventPublisher).publishEvent(any(UserLeaveClanEvent.class));
    }

    @Test
    void leaveClan_MemberNotFound_ThrowsException() {
        when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.findByClanIdAndUsername(CLAN_ID, MEMBER_USER)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            membershipService.leaveClan(CLAN_ID, MEMBER_USER),
            "Should throw IllegalArgumentException when member not found"
        );

        assertEquals("Member tidak ditemukan.", ex.getMessage(), MSG_EXCEPTION_MSG);
    }

    @Test
    void leaveClan_LeaderWithSmallClan_DeletesClan() {
        when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.findByClanIdAndUsername(CLAN_ID, LEADER_USER)).thenReturn(Optional.of(leaderMember));
        when(memberRepository.findByClanId(CLAN_ID)).thenReturn(List.of(leaderMember)); // size is 1 <= MIN_CLAN_SIZE (which is 1)

        membershipService.leaveClan(CLAN_ID, LEADER_USER);

        verify(memberRepository).deleteByClanIdAndUsername(CLAN_ID, LEADER_USER);
        verify(eventPublisher).publishEvent(any(ClanShouldBeDeletedEvent.class));
        verify(eventPublisher).publishEvent(any(UserLeaveClanEvent.class));
    }

    @Test
    void leaveClan_LeaderWithLargerClan_PromotesReplacementLeader() {
        ClanMember otherMember = new ClanMember();
        otherMember.setClanId(CLAN_ID);
        otherMember.setUsername(OTHER_MEMBER_USER);
        otherMember.setRole(ClanRole.MEMBER);

        when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.findByClanIdAndUsername(CLAN_ID, LEADER_USER)).thenReturn(Optional.of(leaderMember));
        List<ClanMember> membersList = List.of(leaderMember, otherMember);
        when(memberRepository.findByClanId(CLAN_ID)).thenReturn(membersList);
        when(clanValidator.resolveReplacementLeader(membersList, LEADER_USER)).thenReturn(OTHER_MEMBER_USER);

        membershipService.leaveClan(CLAN_ID, LEADER_USER);

        verify(clanRepository).save(dummyClan);
        verify(memberRepository).deleteByClanIdAndUsername(CLAN_ID, LEADER_USER);
        verify(eventPublisher).publishEvent(any(UserLeaveClanEvent.class));
        assertEquals(OTHER_MEMBER_USER, dummyClan.getLeaderUsername(), "Leader username should be updated to replacement leader");
    }

    @Test
    void kickMember_Success() {
        when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(dummyClan));

        membershipService.kickMember(CLAN_ID, LEADER_USER, MEMBER_USER);

        verify(clanValidator).requireClanId(CLAN_ID);
        verify(clanValidator).requireUsername(LEADER_USER);
        verify(clanValidator).requireUsername(MEMBER_USER);
        verify(clanValidator).requireLeaderPrivilege(dummyClan, LEADER_USER, "Hanya Leader yang bisa mengeluarkan anggota");
        verify(memberRepository).deleteByClanIdAndUsername(CLAN_ID, MEMBER_USER);
        verify(eventPublisher).publishEvent(any(UserLeaveClanEvent.class));
    }

    @Test
    void kickMember_Self_ThrowsException() {
        when(clanRepository.findById(CLAN_ID)).thenReturn(Optional.of(dummyClan));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            membershipService.kickMember(CLAN_ID, LEADER_USER, LEADER_USER),
            "Should throw IllegalArgumentException when leader kicks self"
        );

        assertEquals("Leader tidak bisa mengeluarkan diri sendiri", ex.getMessage(), MSG_EXCEPTION_MSG);
    }

    @Test
    void getMembersByClanId_Success() {
        when(memberRepository.getClanMembersByClanId(CLAN_ID)).thenReturn(List.of(leaderMember, normalMember));

        List<ClanMember> result = membershipService.getMembersByClanId(CLAN_ID);

        verify(clanValidator).requireClanId(CLAN_ID);
        assertEquals(2, result.size(), MSG_MEMBERS_SIZE);
    }

    @Test
    void deleteAllMembers_Success() {
        membershipService.deleteAllMembers(CLAN_ID);

        verify(clanValidator).requireClanId(CLAN_ID);
        verify(memberRepository).deleteByClanId(CLAN_ID);
    }
}
