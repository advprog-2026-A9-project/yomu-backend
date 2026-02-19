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
class ReadingServiceImplTest {

    @Mock
    private ReadingTextRepository repository;

    @InjectMocks
    private ReadingServiceImpl service;

    @Test
    void testFindAll() {
        // Tambahkan 'final' agar PMD tidak protes 'LocalVariableCouldBeFinal'
        final ReadingText dummyBook = new ReadingText(null, "Buku Tes", "Isi Tes", "Edukasi");

        // Panggil eksplisit menggunakan 'Mockito.' (menghindari static imports)
        Mockito.when(repository.findAll()).thenReturn(List.of(dummyBook));

        // Tambahkan 'final'
        final List<ReadingText> result = service.findAll();

        // Panggil eksplisit menggunakan 'Assertions.'
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals("Buku Tes", result.get(0).getTitle());

        Mockito.verify(repository, Mockito.times(1)).findAll();
    }
}