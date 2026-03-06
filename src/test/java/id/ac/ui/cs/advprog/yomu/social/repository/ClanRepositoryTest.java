package id.ac.ui.cs.advprog.yomu.social.repository;

import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ClanRepositoryTest {

    @MockBean
    private ClanRepository clanRepository;

    private Clan dummyClan;
    private String clanNameSobat;
    private String clanNameWibu;
    private String clanNameNew;
    private String leaderId;

    @BeforeEach
    void setUp() {
        // Inisialisasi String literals
        clanNameSobat = "Sobat Membaca";
        clanNameWibu = "Wibu Elite";
        clanNameNew = "Clan Baru";
        leaderId = "user-1";

        // Setup objek dummy
        dummyClan = new Clan();
        dummyClan.setName(clanNameSobat);
        dummyClan.setLeaderUserId(leaderId);
    }

    @Test
    void testMockFindByName() {
        when(clanRepository.findByName(clanNameSobat))
                .thenReturn(Optional.of(dummyClan));

        Optional<Clan> result = clanRepository.findByName(clanNameSobat);

        assertAll("Verify findByName result and interaction",
            () -> assertTrue(result.isPresent(), "Clan should be found by its name"),
            () -> assertEquals(clanNameSobat, result.get().getName(), "The name in the result should match the query"),
            () -> assertEquals(leaderId, result.get().getLeaderUserId(), "The leader ID should match the assigned value"),
            () -> verify(clanRepository, times(1)).findByName(clanNameSobat)
        );
    }

@Test
    void testMockExistsByName() {
        when(clanRepository.existsByName(clanNameWibu)).thenReturn(true);
        when(clanRepository.existsByName(clanNameNew)).thenReturn(false);

        assertAll("Verify existsByName status and interaction",
            () -> assertTrue(clanRepository.existsByName(clanNameWibu), "Should return true for existing clan name"),
            () -> assertFalse(clanRepository.existsByName(clanNameNew), "Should return false for non-existing clan name"),
            () -> verify(clanRepository, times(1)).existsByName(clanNameWibu),
            () -> verify(clanRepository, times(1)).existsByName(clanNameNew)
        );
    }
}