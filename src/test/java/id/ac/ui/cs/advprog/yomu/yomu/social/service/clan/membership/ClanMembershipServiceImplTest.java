package id.ac.ui.cs.advprog.yomu.social.service.clan.membership;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
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
@SuppressWarnings("null")
class ClanMembershipServiceImplTest {

    private static final String CLAN_123 = "clan-123";
    private static final String LEADER_1 = "leader-1";
    private static final String USER_1 = "user-1";

    private static final String MSG_THROW = "Should throw IllegalArgumentException";
    private static final String MSG_MESSAGE = "Message should match";

    @Mock
    private ClanRepository clanRepository;

    @Mock
    private ClanMemberRepository memberRepository;

    @Mock
    private ClanValidator clanValidator;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ClanMembershipServiceImpl membershipService;

    private Clan dummyClan;
    private ClanMember dummyMember;

    @BeforeEach
    void setUp() {
        membershipService = new ClanMembershipServiceImpl(clanRepository, memberRepository, clanValidator, eventPublisher);

        dummyClan = new Clan();
        dummyClan.setId(CLAN_123);
        dummyClan.setName("Wibu Elite");
        dummyClan.setLeaderUsername(LEADER_1);
        dummyClan.setTier(Tier.BRONZE);

        dummyMember = new ClanMember();
        dummyMember.setClanId(CLAN_123);
        dummyMember.setUsername(USER_1);
        dummyMember.setRole(ClanRole.MEMBER);
    }

    @Test
    void joinClan_WhenClanNotFound_ShouldThrowException() {
        when(clanRepository.findById(CLAN_123)).thenReturn(Optional.empty());

        assertAll("Verify join clan not found exception",
                () -> {
                    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                            () -> membershipService.joinClan(CLAN_123, USER_1, "MEMBER"),
                            MSG_THROW);
                    assertEquals(SocialConstants.CLAN_NOT_FOUND_MESSAGE, ex.getMessage(), MSG_MESSAGE);
                });
    }

    @Test
    void joinClan_WhenValid_ShouldSaveAndPublishEvent() {
        when(clanRepository.findById(CLAN_123)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.findByClanIdAndUsername(CLAN_123, USER_1)).thenReturn(Optional.empty());
        when(memberRepository.findByUsername(USER_1)).thenReturn(Optional.empty());
        when(memberRepository.countByClanId(CLAN_123)).thenReturn(5L);
        when(memberRepository.save(any(ClanMember.class))).thenReturn(dummyMember);

        assertAll("Verify valid join clan saves member and publishes event",
                () -> assertDoesNotThrow(() -> membershipService.joinClan(CLAN_123, USER_1, "MEMBER"),
                        "Should successfully join clan without throwing exception"),
                () -> verify(clanValidator).requireClanId(CLAN_123),
                () -> verify(clanValidator).requireUsername(USER_1),
                () -> verify(clanValidator).requireNotAlreadyMember(false),
                () -> verify(clanValidator).requireNotMemberOfOtherClan(false),
                () -> verify(clanValidator).requireClanNotFull(5L),
                () -> verify(memberRepository).save(any(ClanMember.class)),
                () -> verify(eventPublisher).publishEvent(any(UserJoinClanEvent.class)));
    }

    @Test
    void leaveClan_WhenClanNotFound_ShouldThrowException() {
        when(clanRepository.findById(CLAN_123)).thenReturn(Optional.empty());

        assertAll("Verify leave clan not found exception",
                () -> {
                    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                            () -> membershipService.leaveClan(CLAN_123, USER_1),
                            MSG_THROW);
                    assertEquals(SocialConstants.CLAN_NOT_FOUND_MESSAGE, ex.getMessage(), MSG_MESSAGE);
                });
    }

    @Test
    void leaveClan_WhenMemberNotFound_ShouldThrowException() {
        when(clanRepository.findById(CLAN_123)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.findByClanIdAndUsername(CLAN_123, USER_1)).thenReturn(Optional.empty());

        assertAll("Verify leave clan member not found exception",
                () -> {
                    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                            () -> membershipService.leaveClan(CLAN_123, USER_1),
                            MSG_THROW);
                    assertEquals("Member tidak ditemukan.", ex.getMessage(), MSG_MESSAGE);
                });
    }

    @Test
    void leaveClan_AsMember_ShouldDeleteAndPublishLeaveEvent() {
        when(clanRepository.findById(CLAN_123)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.findByClanIdAndUsername(CLAN_123, USER_1)).thenReturn(Optional.of(dummyMember));

        assertAll("Verify member can leave clan",
                () -> assertDoesNotThrow(() -> membershipService.leaveClan(CLAN_123, USER_1),
                        "Should leave clan as member without throwing exception"),
                () -> verify(memberRepository).deleteByClanIdAndUsername(CLAN_123, USER_1),
                () -> verify(eventPublisher).publishEvent(any(UserLeaveClanEvent.class)));
    }

    @Test
    void leaveClan_AsLeader_WhenLastMember_ShouldDeleteClan() {
        ClanMember leader = new ClanMember();
        leader.setClanId(CLAN_123);
        leader.setUsername(LEADER_1);
        leader.setRole(ClanRole.LEADER);

        when(clanRepository.findById(CLAN_123)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.findByClanIdAndUsername(CLAN_123, LEADER_1)).thenReturn(Optional.of(leader));
        when(memberRepository.findByClanId(CLAN_123)).thenReturn(List.of(leader));

        assertAll("Verify leader leaves as last member deletes clan",
                () -> assertDoesNotThrow(() -> membershipService.leaveClan(CLAN_123, LEADER_1),
                        "Should leave clan as leader when last member without throwing exception"),
                () -> verify(memberRepository).deleteByClanIdAndUsername(CLAN_123, LEADER_1),
                () -> verify(eventPublisher).publishEvent(any(ClanShouldBeDeletedEvent.class)),
                () -> verify(eventPublisher).publishEvent(any(UserLeaveClanEvent.class)));
    }

    @Test
    void leaveClan_AsLeader_WhenOtherMembersExist_ShouldPromoteReplacement() {
        ClanMember leader = new ClanMember();
        leader.setClanId(CLAN_123);
        leader.setUsername(LEADER_1);
        leader.setRole(ClanRole.LEADER);

        ClanMember member = new ClanMember();
        member.setClanId(CLAN_123);
        member.setUsername("user-2");
        member.setRole(ClanRole.MEMBER);

        List<ClanMember> list = List.of(leader, member);

        when(clanRepository.findById(CLAN_123)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.findByClanIdAndUsername(CLAN_123, LEADER_1)).thenReturn(Optional.of(leader));
        when(memberRepository.findByClanId(CLAN_123)).thenReturn(list);
        when(clanValidator.resolveReplacementLeader(list, LEADER_1)).thenReturn("user-2");
        when(clanRepository.save(dummyClan)).thenReturn(dummyClan);

        membershipService.leaveClan(CLAN_123, LEADER_1);

        assertAll("Verify new leader assignment",
                () -> assertEquals("user-2", dummyClan.getLeaderUsername(), "Leader username should be updated to user-2"),
                () -> verify(clanRepository).save(dummyClan),
                () -> verify(memberRepository).deleteByClanIdAndUsername(CLAN_123, LEADER_1),
                () -> verify(eventPublisher).publishEvent(any(UserLeaveClanEvent.class)));
    }

    @Test
    void kickMember_WhenClanNotFound_ShouldThrowException() {
        when(clanRepository.findById(CLAN_123)).thenReturn(Optional.empty());

        assertAll("Verify kick member clan not found exception",
                () -> {
                    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                            () -> membershipService.kickMember(CLAN_123, LEADER_1, USER_1),
                            MSG_THROW);
                    assertEquals(SocialConstants.CLAN_NOT_FOUND_MESSAGE, ex.getMessage(), MSG_MESSAGE);
                });
    }

    @Test
    void kickMember_WhenSelfKick_ShouldThrowException() {
        when(clanRepository.findById(CLAN_123)).thenReturn(Optional.of(dummyClan));

        assertAll("Verify self kick exception",
                () -> {
                    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                            () -> membershipService.kickMember(CLAN_123, LEADER_1, LEADER_1),
                            MSG_THROW);
                    assertEquals("Leader tidak bisa mengeluarkan diri sendiri", ex.getMessage(), MSG_MESSAGE);
                });
    }

    @Test
    void kickMember_WhenValid_ShouldDeleteAndPublishEvent() {
        when(clanRepository.findById(CLAN_123)).thenReturn(Optional.of(dummyClan));

        assertAll("Verify valid kick deletes member and publishes event",
                () -> assertDoesNotThrow(() -> membershipService.kickMember(CLAN_123, LEADER_1, USER_1),
                        "Should kick member without throwing exception"),
                () -> verify(clanValidator).requireLeaderPrivilege(eq(dummyClan), eq(LEADER_1), anyString()),
                () -> verify(memberRepository).deleteByClanIdAndUsername(CLAN_123, USER_1),
                () -> verify(eventPublisher).publishEvent(any(UserLeaveClanEvent.class)));
    }

    @Test
    void getMembersByClanId_ShouldReturnList() {
        when(memberRepository.getClanMembersByClanId(CLAN_123)).thenReturn(List.of(dummyMember));

        List<ClanMember> result = membershipService.getMembersByClanId(CLAN_123);

        assertAll("Verify member retrieval details",
                () -> assertEquals(1, result.size(), "Result size should be 1"),
                () -> assertEquals(USER_1, result.get(0).getUsername(), "Username of member should match"),
                () -> verify(clanValidator).requireClanId(CLAN_123));
    }

    @Test
    void deleteAllMembers_ShouldCallRepository() {
        doNothing().when(memberRepository).deleteByClanId(CLAN_123);

        assertAll("Verify delete all members calls repository",
                () -> assertDoesNotThrow(() -> membershipService.deleteAllMembers(CLAN_123),
                        "Should delete all members without throwing exception"),
                () -> verify(clanValidator).requireClanId(CLAN_123),
                () -> verify(memberRepository).deleteByClanId(CLAN_123));
    }
}
