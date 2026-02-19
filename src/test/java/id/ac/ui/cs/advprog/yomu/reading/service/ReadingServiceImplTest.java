package id.ac.ui.cs.advprog.yomu.reading.service;

import id.ac.ui.cs.advprog.yomu.reading.model.ReadingText;
import id.ac.ui.cs.advprog.yomu.reading.repository.ReadingTextRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
// Bungkam PMD khusus untuk aturan "max 1 assert per test"
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
class ReadingServiceImplTest {

    @Mock
    private ReadingTextRepository repository;

    @InjectMocks
    private ReadingServiceImpl service;

    @Test
    void testFindAll() {
        final ReadingText dummyBook = new ReadingText(null, "Buku Tes", "Isi Tes", "Edukasi");

        Mockito.when(repository.findAll()).thenReturn(List.of(dummyBook));

        final List<ReadingText> result = service.findAll();

        // Tambahkan pesan di argumen terakhir agar PMD bahagia
        Assertions.assertFalse(result.isEmpty(), "Result list should not be empty");
        Assertions.assertEquals("Buku Tes", result.get(0).getTitle(), "Title should match the dummy book");

        Mockito.verify(repository, Mockito.times(1)).findAll();
    }
}