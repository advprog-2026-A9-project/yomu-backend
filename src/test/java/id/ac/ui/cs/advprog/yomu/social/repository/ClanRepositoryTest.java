package id.ac.ui.cs.advprog.yomu.social.repository;

import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClanRepositoryTest {

    @Mock
    private ClanRepository clanRepository;

    private Clan dummyClan;
    private String clanNameSobat;
    private String clanNameWibu;
    private String clanNameNew;
    private String leaderId;

    @BeforeEach
    void setUp() {
        clanNameSobat = "Sobat Membaca";
        clanNameWibu = "Wibu Elite";
        clanNameNew = "Clan Baru";
        leaderId = "user-1";

        dummyClan = new Clan();
        dummyClan.setName(clanNameSobat);
        dummyClan.setLeaderUsername(leaderId);
    }

    @Test
    void testMockFindByName() {
        // Mocking tetap sama
        when(clanRepository.findByName(clanNameSobat)).thenReturn(Optional.of(dummyClan));

        Optional<Clan> result = clanRepository.findByName(clanNameSobat);

        assertAll("Verify findByName",
                () -> assertTrue(result.isPresent(), "Verify clan exists"),
                () -> assertEquals(clanNameSobat, result.get().getName(), "Verify name match"),
                () -> verify(clanRepository, times(1)).findByName(clanNameSobat));
    }

    @Test
    void testMockExistsByName() {
        when(clanRepository.existsByName(clanNameWibu)).thenReturn(true);
        when(clanRepository.existsByName(clanNameNew)).thenReturn(false);

        assertAll("Verify existsByName",
                () -> assertTrue(clanRepository.existsByName(clanNameWibu), "verify clan exists"),
                () -> assertFalse(clanRepository.existsByName(clanNameNew), "verify clan doesn't exists"),
                () -> verify(clanRepository, times(1)).existsByName(clanNameWibu),
                () -> verify(clanRepository, times(1)).existsByName(clanNameNew));
    }
}