package id.ac.ui.cs.advprog.yomu.social.repository;

import id.ac.ui.cs.advprog.yomu.social.model.ClanMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClanMemberRepositoryTest {

    @Mock
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
        member123.setUsername(userId456);

        member1 = new ClanMember();
        member1.setClanId(clanId1);
        member1.setUsername(userId1);
    }

    @Test
    void testMockFindByClanId() {
        when(clanMemberRepository.findByClanId(clanId123))
                .thenReturn(Arrays.asList(member123));

        List<ClanMember> result = clanMemberRepository.findByClanId(clanId123);

        assertAll("Verify findByClanId results and interaction",
                // Ganti getUserId() -> getUsername()
                () -> assertEquals(1, result.size(), "Find One Clan"),
                () -> assertEquals(userId456, result.get(0).getUsername(), "Verify member Username"),
                () -> verify(clanMemberRepository, times(1)).findByClanId(clanId123));
    }

    @Test
    void testMockFindByClanIdAndUsername() {
        when(clanMemberRepository.findByClanIdAndUsername(clanId1, userId1))
                .thenReturn(Optional.of(member1));

        Optional<ClanMember> result = clanMemberRepository.findByClanIdAndUsername(clanId1, userId1);

        assertAll("Verify findByClanIdAndUsername results",
                () -> assertTrue(result.isPresent(), "Verify member exists"),
                () -> assertEquals(clanId1, result.get().getClanId(), "Verify Clan Match"),
                () -> verify(clanMemberRepository, times(1)).findByClanIdAndUsername(clanId1, userId1));
    }

    @Test
    void testMockDeleteByClanIdAndUsername() {
        doNothing().when(clanMemberRepository).deleteByClanIdAndUsername(clanId1, userId1);

        clanMemberRepository.deleteByClanIdAndUsername(clanId1, userId1);

        verify(clanMemberRepository, times(1)).deleteByClanIdAndUsername(clanId1, userId1);
    }
}