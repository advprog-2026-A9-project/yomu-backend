package id.ac.ui.cs.advprog.yomu.reading.service;

import id.ac.ui.cs.advprog.yomu.reading.model.ReadingText;
import id.ac.ui.cs.advprog.yomu.reading.repository.ReadingTextRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReadingServiceImplTest {

    @Mock
    private ReadingTextRepository repository;

    @InjectMocks
    private ReadingServiceImpl service;

    @Test
    void testFindAll() {
        // Arrange: Siapkan data palsu (mock)
        ReadingText dummyBook = new ReadingText(null, "Buku Tes", "Isi Tes", "Edukasi");
        when(repository.findAll()).thenReturn(Arrays.asList(dummyBook));

        // Act: Panggil method yang ingin dites
        List<ReadingText> result = service.findAll();

        // Assert: Pastikan hasilnya sesuai ekspektasi
        assertFalse(result.isEmpty());
        assertEquals("Buku Tes", result.get(0).getTitle());

        // Verifikasi bahwa repository.findAll() benar-benar dipanggil 1 kali
        verify(repository, times(1)).findAll();
    }
}