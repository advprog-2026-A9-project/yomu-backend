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
import org.mockito.InjectMocks;
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
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanRepository;
import id.ac.ui.cs.advprog.yomu.social.validation.ClanValidator;
import id.ac.ui.cs.advprog.yomu.social.mapper.SocialMapper;
import static org.mockito.Mockito.lenient;

import org.springframework.context.ApplicationEventPublisher;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class ClanServiceImplTest {

    @Mock
    private ClanRepository clanRepository;

    @Mock
    private ClanMemberRepository memberRepository;

    @Mock
    private ClanValidator clanValidation;

    @Mock
    private SocialMapper socialMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ClanServiceImpl clanService;

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
        dummyClan.setLeaderUserId(leaderId);

        clanRequest = new ClanRequest();
        clanRequest.setName(clanName);
        clanRequest.setUserId(leaderId);
        clanRequest.setUsername(username);
        clanRequest.setDescription(description);

        ClanMember m1 = new ClanMember();
        m1.setUserId(leaderId);
        ClanMember m2 = new ClanMember();
        m2.setUserId(memberId);
        dummyMemberList = Arrays.asList(m1, m2);

        lenient().when(socialMapper.toMyClanResponse(any(), anyString(), any(Integer.class), any()))
                .thenAnswer(invocation -> {
                    Clan c = invocation.getArgument(0);
                    String role = invocation.getArgument(1);
                    int rank = invocation.getArgument(2);
                    List<ClanMember> members = invocation.getArgument(3);
                    return new MyClanResponse(c.getId(), c.getName(), c.getDescription(), c.getLeaderUserId(), role, "Bronze", 0, rank, members);
                });
    }

    @Test
    void testCreateClan_ShouldReturnCorrectData() {
        when(clanRepository.existsByName(anyString())).thenReturn(false);
        when(clanRepository.save(any(Clan.class))).thenReturn(dummyClan);
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.findByClanIdAndUserId(anyString(), anyString())).thenReturn(Optional.empty());
        when(memberRepository.findByUserId(anyString())).thenReturn(Optional.empty());

        Clan created = clanService.createClan(clanRequest);

        assertAll("Verify created clan state",
                () -> assertNotNull(created, "Clan successfull created"),
                () -> assertEquals(clanName, created.getName(), "Match clan name"));
    }

    @Test
    void testCreateClan_ShouldTriggerRepositories() {
        when(clanRepository.existsByName(anyString())).thenReturn(false);
        when(clanRepository.save(any(Clan.class))).thenReturn(dummyClan);
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.findByClanIdAndUserId(anyString(), anyString())).thenReturn(Optional.empty());
        when(memberRepository.findByUserId(anyString())).thenReturn(Optional.empty());

        clanService.createClan(clanRequest);

        assertAll("Verify repository save calls",
                () -> verify(clanRepository, times(1)).save(any(Clan.class)),
                () -> verify(memberRepository, times(1)).save(any(ClanMember.class)));
    }

    @Test
    void testJoinClan_AlreadyInClan_ShouldThrowException() {
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.findByClanIdAndUserId(anyString(), anyString())).thenReturn(Optional.empty());
        when(memberRepository.findByUserId(memberId)).thenReturn(Optional.of(new ClanMember()));

        // Mocking the validation failure
        doThrow(new IllegalStateException(SocialConstants.ALREADY_IN_OTHER_CLAN_MESSAGE))
                .when(clanValidation).requireNotMemberOfOtherClan(true);

        assertThrows(IllegalStateException.class,
                () -> clanService.joinClan(clanId, memberId, username, "MEMBER"));
    }

    @Test
    void testLeaveClan_AsMember_ShouldOnlyDeleteMembership() {
        String randomMember = "user-member";
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));

        clanService.leaveClan(clanId, randomMember);

        verify(memberRepository).deleteByClanIdAndUserId(clanId, randomMember);
    }

    @Test
    void testLeaveClan_AsLeader_WithSuccession() {
        ClanMember leader = new ClanMember();
        leader.setUserId(leaderId);
        ClanMember other = new ClanMember();
        other.setUserId(memberId);

        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.findByClanId(clanId)).thenReturn(Arrays.asList(leader, other));
        when(clanValidation.resolveReplacementLeader(any(), eq(leaderId))).thenReturn(memberId);

        clanService.leaveClan(clanId, leaderId);

        assertAll("Verify leader succession",
                () -> assertEquals(memberId, dummyClan.getLeaderUserId(), "Verify member promoted to leader"),
                () -> verify(clanRepository).save(dummyClan),
                () -> verify(memberRepository).deleteByClanIdAndUserId(clanId, leaderId));
    }

    @Test
    void testLeaveClan_AsLastLeader_ShouldDeleteClan() {
        ClanMember leader = new ClanMember();
        leader.setUserId(leaderId);

        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.findByClanId(clanId)).thenReturn(Arrays.asList(leader));

        clanService.leaveClan(clanId, leaderId);

        assertAll("Verify clan deletion",
                () -> verify(memberRepository).deleteByClanIdAndUserId(clanId, leaderId),
                () -> verify(clanRepository).delete(dummyClan));
    }

    @Test
    void testGetMyClanByUserId_AsLeader_ShouldReturnCorrectData() {
        ClanMember membership = new ClanMember();
        membership.setClanId(clanId);
        membership.setUserId(leaderId);

        when(memberRepository.findByUserId(leaderId)).thenReturn(Optional.of(membership));
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.getClanMembersByClanId(clanId)).thenReturn(dummyMemberList);

        Optional<MyClanResponse> result = clanService.getMyClanByUserId(leaderId);

        assertAll("Verify response for leader",
                () -> assertTrue(result.isPresent(), "Verify clan exists"),
                () -> assertEquals(SocialConstants.MY_CLAN_ROLE_LEADER, result.get().role(), "Verify role is leader"),
                () -> assertEquals(2, result.get().members().size(), "Verify member amount"));
    }

    @Test
    void testGetMyClanByUserId_AsMember_ShouldReturnCorrectData() {
        ClanMember membership = new ClanMember();
        membership.setClanId(clanId);
        membership.setUserId(memberId);

        when(memberRepository.findByUserId(memberId)).thenReturn(Optional.of(membership));
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.getClanMembersByClanId(clanId)).thenReturn(List.of(membership));

        Optional<MyClanResponse> result = clanService.getMyClanByUserId(memberId);

        assertAll("Verify response for member",
                () -> assertTrue(result.isPresent(), "Verify clan exists"),
                () -> assertEquals(SocialConstants.MY_CLAN_ROLE_MEMBER, result.get().role(), "Verify role is member"),
                () -> assertEquals(1, result.get().members().size(), "Verify member amount"));
    }

    @Test
    void testGetMyClanByUserId_WhenNoMembership_ShouldReturnEmpty() {
        when(memberRepository.findByUserId(memberId)).thenReturn(Optional.empty());

        Optional<MyClanResponse> result = clanService.getMyClanByUserId(memberId);

        assertTrue(result.isEmpty(), "Verify no member returned");
    }
}