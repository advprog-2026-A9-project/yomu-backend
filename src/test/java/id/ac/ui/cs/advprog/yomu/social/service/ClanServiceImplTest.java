package id.ac.ui.cs.advprog.yomu.social.service;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
import id.ac.ui.cs.advprog.yomu.social.dto.MyClanResponse;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanMemberRepository;
import id.ac.ui.cs.advprog.yomu.social.repository.ClanRepository;

@ExtendWith(MockitoExtension.class)
class ClanServiceImplTest {

    @Mock
    private ClanRepository clanRepository;

    @Mock
    private ClanMemberRepository memberRepository;

    @InjectMocks
    private ClanServiceImpl clanService;

    private Clan dummyClan;
    private ClanRequest clanRequest;

    private String clanId;
    private String leaderId;
    private String memberId;
    private String clanName;
    private String description;

    @BeforeEach
    void setUp() {
        clanId = "clan-1";
        leaderId = "leader-1";
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
        clanRequest.setDescription(description);
    }

    @Test
    void testCreateClan_ShouldReturnCorrectData() {
        when(clanRepository.existsByName(anyString())).thenReturn(false);
        when(clanRepository.save(any(Clan.class))).thenReturn(dummyClan);
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));

        Clan created = clanService.createClan(clanRequest);

        assertAll("Verify created clan state",
            () -> assertNotNull(created, "Created clan should not be null"),
            () -> assertEquals(clanName, created.getName(), "Clan name should match the request")
        );
    }

    @Test
    void testCreateClan_ShouldTriggerRepositories() {
        when(clanRepository.existsByName(anyString())).thenReturn(false);
        when(clanRepository.save(any(Clan.class))).thenReturn(dummyClan);
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));

        clanService.createClan(clanRequest);

        assertAll("Verify repository interactions",
            () -> verify(clanRepository, times(1)).save(any(Clan.class)),
            () -> verify(memberRepository, times(1)).save(any(ClanMember.class))
        );
    }

    @Test
    void testJoinClan_AlreadyInClan_ShouldThrowException() {
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.findByUserId(memberId)).thenReturn(Optional.of(new ClanMember()));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> clanService.joinClan(clanId, memberId));
        assertEquals("Kamu sudah tergabung di Clan lain", exception.getMessage(), 
            "Exception message should indicate user already in another clan");
    }

    @Test
    void testLeaveClan_AsMember_ShouldOnlyDeleteMembership() {
        String randomMember = "user-member";
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));

        clanService.leaveClan(clanId, randomMember);

        assertAll("Verify membership deletion only",
            () -> verify(memberRepository).deleteByClanIdAndUserId(clanId, randomMember),
            () -> verify(clanRepository, never()).delete(any())
        );
    }

    @Test
    void testLeaveClan_AsLeader_WithSuccession() {
        ClanMember leader = new ClanMember(); 
        leader.setUserId(leaderId);
        ClanMember other = new ClanMember(); 
        other.setUserId(memberId);

        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.findByClanId(clanId)).thenReturn(Arrays.asList(leader, other));

        clanService.leaveClan(clanId, leaderId);

        assertAll("Verify leadership succession",
            () -> assertEquals(memberId, dummyClan.getLeaderUserId(), "Leadership should be transferred"),
            () -> verify(clanRepository).save(dummyClan),
            () -> verify(memberRepository).deleteByClanIdAndUserId(clanId, leaderId)
        );
    }

    @Test
    void testLeaveClan_AsLastLeader_ShouldDeleteClan() {
        ClanMember leader = new ClanMember(); 
        leader.setUserId(leaderId);

        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.findByClanId(clanId)).thenReturn(Arrays.asList(leader));

        clanService.leaveClan(clanId, leaderId);

        assertAll("Verify clan deletion",
            () -> verify(clanRepository).delete(dummyClan),
            () -> verify(memberRepository).deleteByClanIdAndUserId(clanId, leaderId)
        );
    }

    @Test
    void testGetMyClanByUserId_AsLeader_ShouldReturnKetuaRole() {
        ClanMember membership = new ClanMember();
        membership.setClanId(clanId);
        membership.setUserId(leaderId);
        when(memberRepository.findByUserId(leaderId)).thenReturn(Optional.of(membership));
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.countByClanId(clanId)).thenReturn(2L);

        Optional<MyClanResponse> result = clanService.getMyClanByUserId(leaderId);

        assertTrue(result.isPresent(), "Leader should have a clan");
        assertEquals("KETUA", result.get().role(), "Leader role should be KETUA");
        assertEquals(2, result.get().members(), "Clan should have 2 members");
    }

    @Test
    void testGetMyClanByUserId_AsMember_ShouldReturnAnggotaRole() {
        ClanMember membership = new ClanMember();
        membership.setClanId(clanId);
        membership.setUserId(memberId);

        when(memberRepository.findByUserId(memberId)).thenReturn(Optional.of(membership));
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(dummyClan));
        when(memberRepository.countByClanId(clanId)).thenReturn(1L);

        Optional<MyClanResponse> result = clanService.getMyClanByUserId(memberId);

        assertTrue(result.isPresent(), "Member should have a clan");
        assertEquals("ANGGOTA", result.get().role(), "Member role should be ANGGOTA");
        assertEquals(1, result.get().members(), "Clan should have 1 member");
    }

    @Test
    void testGetMyClanByUserId_WhenNoMembership_ShouldReturnEmpty() {
        when(memberRepository.findByUserId(memberId)).thenReturn(Optional.empty());

        Optional<MyClanResponse> result = clanService.getMyClanByUserId(memberId);

        assertTrue(result.isEmpty(), "User with no membership should have empty result");
        verify(clanRepository, never()).findById(anyString());
    }
}