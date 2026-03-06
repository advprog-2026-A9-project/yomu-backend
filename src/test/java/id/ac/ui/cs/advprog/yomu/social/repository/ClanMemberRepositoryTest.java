package id.ac.ui.cs.advprog.yomu.social.repository;

import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ClanMemberRepositoryTest {

    @MockBean
    private ClanMemberRepository clanMemberRepository;

    private String clanId123;
    private String userId456;
    private String clanId1;
    private String userId1;
    private ClanMember member123;
    private ClanMember member1;

    @BeforeEach
    void setUp() {
        // Inisialisasi String literals
        clanId123 = "clan-123";
        userId456 = "user-456";
        clanId1 = "clan-1";
        userId1 = "user-1";

        // Setup objek dummy
        member123 = new ClanMember();
        member123.setClanId(clanId123);
        member123.setUserId(userId456);

        member1 = new ClanMember();
        member1.setClanId(clanId1);
        member1.setUserId(userId1);
    }

    @Test
    void testMockFindByClanId() {
        when(clanMemberRepository.findByClanId(clanId123))
                .thenReturn(Arrays.asList(member123));

        List<ClanMember> result = clanMemberRepository.findByClanId(clanId123);

        assertAll("Verify findByClanId results and interaction",
            () -> assertEquals(1, result.size(), "The result list size should be 1"),
            () -> assertEquals(userId456, result.get(0).getUserId(), "The User ID should match"),
            () -> verify(clanMemberRepository, times(1)).findByClanId(clanId123)
        );
    }

    @Test
    void testMockFindByClanIdAndUserId() {
        when(clanMemberRepository.findByClanIdAndUserId(clanId1, userId1))
                .thenReturn(Optional.of(member1));

        Optional<ClanMember> result = clanMemberRepository.findByClanIdAndUserId(clanId1, userId1);

        assertAll("Verify findByClanIdAndUserId results",
            () -> assertTrue(result.isPresent(), "The result should contain a ClanMember object"),
            () -> assertEquals(clanId1, result.get().getClanId(), "The Clan ID should match"),
            () -> verify(clanMemberRepository, times(1)).findByClanIdAndUserId(clanId1, userId1)
        );
    }

    @Test
    void testMockDeleteByClanIdAndUserId() {
        doNothing().when(clanMemberRepository).deleteByClanIdAndUserId(clanId1, userId1);

        clanMemberRepository.deleteByClanIdAndUserId(clanId1, userId1);

        verify(clanMemberRepository, times(1)).deleteByClanIdAndUserId(clanId1, userId1);
    }
}