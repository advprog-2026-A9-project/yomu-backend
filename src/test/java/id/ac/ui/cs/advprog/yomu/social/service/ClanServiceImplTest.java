package id.ac.ui.cs.advprog.yomu.social.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;

import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
import id.ac.ui.cs.advprog.yomu.social.dto.MyClanResponse;
import id.ac.ui.cs.advprog.yomu.social.event.ClanCreatedEvent;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanJoinRequestRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanRepository;
import id.ac.ui.cs.advprog.yomu.social.validation.ClanValidator;
import id.ac.ui.cs.advprog.yomu.social.mapper.SocialMapper;
import static org.mockito.Mockito.lenient;

import org.springframework.context.ApplicationEventPublisher;
import id.ac.ui.cs.advprog.yomu.social.service.clan.lifecycle.ClanLifecycleServiceImpl;
import id.ac.ui.cs.advprog.yomu.social.service.clan.membership.ClanMembershipServiceImpl;
import id.ac.ui.cs.advprog.yomu.social.service.clan.query.ClanQueryServiceImpl;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class ClanServiceImplTest {

    @Mock
    private ClanRepository clanRepository;

    @Mock
    private ClanMemberRepository memberRepository;

    @Mock
    private ClanJoinRequestRepository joinRequestRepository;

    @Mock
    private ClanValidator clanValidation;

    @Mock
    private SocialMapper socialMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ClanLifecycleServiceImpl lifecycleService;
    private ClanMembershipServiceImpl membershipService;
    private ClanQueryServiceImpl queryService;

    private Clan dummyClan;
    private ClanRequest clanRequest;
    private List<ClanMember> dummyMemberList;

    private String clanId;
    private String leaderId;
    private String memberId;
    private String clanName;
    private String description;
    private String username;

    @BeforeEach
    void setUp() {
        clanId = "clan-1";
        leaderId = "leader-1";
        username = "wibu";
        memberId = "user-2";
        clanName = "Wibu Elite";
        description = "Clan untuk pecinta buku.";

        dummyClan = new Clan();
        dummyClan.setId(clanId);
        dummyClan.setName(clanName);
        dummyClan.setLeaderUsername(leaderId);
        dummyClan.setTier(id.ac.ui.cs.advprog.yomu.social.model.Tier.BRONZE);
        dummyClan.setScore(0);

        clanRequest = new ClanRequest();
        clanRequest.setName(clanName);
        clanRequest.setUsername(username);
        clanRequest.setDescription(description);

        ClanMember m1 = new ClanMember();
        m1.setUsername(leaderId);
        ClanMember m2 = new ClanMember();
        m2.setUsername(memberId);
        dummyMemberList = Arrays.asList(m1, m2);

        lenient().when(socialMapper.toMyClanResponse(any(), anyString(), any(Integer.class), any()))
                .thenAnswer(invocation -> {
                    Clan c = invocation.getArgument(0);
                    String role = invocation.getArgument(1);
                    int rank = invocation.getArgument(2);
                    List<ClanMember> members = invocation.getArgument(3);
                    return new MyClanResponse(c.getId(), c.getName(), c.getDescription(), c.getLeaderUsername(), role,
                            "Bronze", 0, rank, members);
                });

        lifecycleService = new ClanLifecycleServiceImpl(clanRepository, memberRepository, clanValidation,
                eventPublisher);
        membershipService = new ClanMembershipServiceImpl(clanRepository, memberRepository, clanValidation,
                eventPublisher);
        queryService = new ClanQueryServiceImpl(clanRepository, memberRepository, clanValidation, socialMapper, null);
    }

    @Test
    void testCreateClan_ShouldReturnCorrectData() {
        lenient().when(clanRepository.existsByName(anyString())).thenReturn(false);
        lenient().when(clanRepository.save(any(Clan.class))).thenReturn(dummyClan);
        lenient().when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));
        lenient().when(memberRepository.findByClanIdAndUsername(anyString(), anyString())).thenReturn(Optional.empty());
        lenient().when(memberRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        Clan created = lifecycleService.createClan(clanRequest);

        assertAll("Verify created clan state",
                () -> assertNotNull(created, "Clan successfull created"),
                () -> assertEquals(clanName, created.getName(), "Match clan name"));
    }

    @Test
    void testCreateClan_ShouldTriggerRepositories() {
        lenient().when(clanRepository.existsByName(anyString())).thenReturn(false);
        lenient().when(clanRepository.save(any(Clan.class))).thenReturn(dummyClan);
        lenient().when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));
        lenient().when(memberRepository.findByClanIdAndUsername(anyString(), anyString())).thenReturn(Optional.empty());
        lenient().when(memberRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        lifecycleService.createClan(clanRequest);

        assertAll("Verify repository save calls",
                () -> verify(clanRepository, times(1)).save(any(Clan.class)),
                () -> verify(eventPublisher, times(1)).publishEvent(any(ClanCreatedEvent.class)));
    }

    @Test
    void testJoinClan_AlreadyInClan_ShouldThrowException() {
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.findByClanIdAndUsername(anyString(), anyString())).thenReturn(Optional.empty());
        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(new ClanMember()));

        // Mocking the validation failure
        doThrow(new IllegalStateException(SocialConstants.ALREADY_IN_OTHER_CLAN_MESSAGE))
                .when(clanValidation).requireNotMemberOfOtherClan(true);

        assertThrows(IllegalStateException.class,
                () -> membershipService.joinClan(clanId, username, "MEMBER"));
    }

    @Test
    void testLeaveClan_AsMember_ShouldOnlyDeleteMembership() {
        String randomMember = "user-member";
        ClanMember dummyMember = new ClanMember();
        dummyMember.setClanId(clanId);
        dummyMember.setUsername(randomMember);
        dummyMember.setRole(SocialConstants.ROLE_MEMBER);

        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.findByClanIdAndUsername(clanId, randomMember)).thenReturn(Optional.of(dummyMember));

        membershipService.leaveClan(clanId, randomMember);

        verify(memberRepository).deleteByClanIdAndUsername(clanId, randomMember);
    }

    @Test
    void testLeaveClan_AsLeader_WithSuccession() {
        ClanMember leader = new ClanMember();
        leader.setUsername(leaderId);
        leader.setRole(SocialConstants.ROLE_LEADER);
        ClanMember other = new ClanMember();
        other.setUsername(memberId);
        other.setRole(SocialConstants.ROLE_MEMBER);

        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.findByClanId(clanId)).thenReturn(Arrays.asList(leader, other));
        when(memberRepository.findByClanIdAndUsername(clanId, leaderId)).thenReturn(Optional.of(leader));
        when(clanValidation.resolveReplacementLeader(any(), eq(leaderId))).thenReturn(memberId);

        membershipService.leaveClan(clanId, leaderId);

        assertAll("Verify leader succession",
                () -> assertEquals(memberId, dummyClan.getLeaderUsername(), "Verify member promoted to leader"),
                () -> verify(clanRepository).save(dummyClan),
                () -> verify(memberRepository).deleteByClanIdAndUsername(clanId, leaderId));
    }

    @Test
    void testLeaveClan_AsLastLeader_ShouldDeleteClan() {
        ClanMember leader = new ClanMember();
        leader.setUsername(leaderId);
        leader.setRole(SocialConstants.ROLE_LEADER);

        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.findByClanId(clanId)).thenReturn(Arrays.asList(leader));
        when(memberRepository.findByClanIdAndUsername(clanId, leaderId)).thenReturn(Optional.of(leader));

        membershipService.leaveClan(clanId, leaderId);

        assertAll("Verify clan deletion triggered via event",
                () -> verify(memberRepository).deleteByClanIdAndUsername(clanId, leaderId),
                () -> verify(eventPublisher)
                        .publishEvent(any(id.ac.ui.cs.advprog.yomu.social.event.ClanShouldBeDeletedEvent.class)));
    }

    @Test
    void testGetMyClanByUsername_AsLeader_ShouldReturnCorrectData() {
        ClanMember membership = new ClanMember();
        membership.setClanId(clanId);
        membership.setUsername(leaderId);

        when(memberRepository.findByUsername(leaderId)).thenReturn(Optional.of(membership));
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.getClanMembersByClanId(clanId)).thenReturn(dummyMemberList);

        Optional<MyClanResponse> result = queryService.getMyClanByUsername(leaderId);

        assertAll("Verify response for leader",
                () -> assertTrue(result.isPresent(), "Verify clan exists"),
                () -> assertEquals(SocialConstants.MY_CLAN_ROLE_LEADER, result.get().role(), "Verify role is leader"),
                () -> assertEquals(2, result.get().members().size(), "Verify member amount"));
    }

    @Test
    void testGetMyClanByUsername_AsMember_ShouldReturnCorrectData() {
        ClanMember membership = new ClanMember();
        membership.setClanId(clanId);
        membership.setUsername(memberId);

        when(memberRepository.findByUsername(memberId)).thenReturn(Optional.of(membership));
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.getClanMembersByClanId(clanId)).thenReturn(List.of(membership));

        Optional<MyClanResponse> result = queryService.getMyClanByUsername(memberId);

        assertAll("Verify response for member",
                () -> assertTrue(result.isPresent(), "Verify clan exists"),
                () -> assertEquals(SocialConstants.MY_CLAN_ROLE_MEMBER, result.get().role(), "Verify role is member"),
                () -> assertEquals(1, result.get().members().size(), "Verify member amount"));
    }

    @Test
    void testGetMyClanByUsername_WhenNoMembership_ShouldReturnEmpty() {
        when(memberRepository.findByUsername(memberId)).thenReturn(Optional.empty());

        Optional<MyClanResponse> result = queryService.getMyClanByUsername(memberId);

        assertTrue(result.isEmpty(), "Verify no member returned");
    }
}