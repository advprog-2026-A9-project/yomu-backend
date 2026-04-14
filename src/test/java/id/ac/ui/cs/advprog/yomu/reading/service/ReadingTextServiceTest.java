package id.ac.ui.cs.advprog.yomu.reading.service;

import id.ac.ui.cs.advprog.yomu.reading.dto.ReadingTextRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.ReadingTextResponse;
import id.ac.ui.cs.advprog.yomu.reading.model.Category;
import id.ac.ui.cs.advprog.yomu.reading.model.ReadingText;
import id.ac.ui.cs.advprog.yomu.reading.repository.CategoryRepository;
import id.ac.ui.cs.advprog.yomu.reading.repository.ReadingTextRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts") // Mengabaikan aturan max 1 assert
class ReadingTextServiceTest {

    // Menyimpan String yang berulang menjadi konstanta (Mengatasi AvoidDuplicateLiterals)
    private static final String TITLE_JAVA = "Belajar Java";
    private static final String CONTENT_DUMMY = "Isi teks...";
    private static final String CATEGORY_EDUKASI = "Edukasi";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_STUDENT = "STUDENT";

    @Mock
    private ReadingTextRepository readingTextRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ReadingTextServiceImpl readingTextService;

    @Test
    void createText_WhenRoleIsAdmin_ShouldReturnSavedText() {
        ReadingTextRequest request = new ReadingTextRequest(TITLE_JAVA, CONTENT_DUMMY, 1L);
        Category category = new Category(1L, CATEGORY_EDUKASI);
        ReadingText savedText = new ReadingText(1L, TITLE_JAVA, CONTENT_DUMMY, category);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(readingTextRepository.save(any(ReadingText.class))).thenReturn(savedText);

        ReadingTextResponse response = readingTextService.createText(request, ROLE_ADMIN);

        // Menambahkan pesan khusus di setiap assert (Mengatasi JUnitAssertionsShouldIncludeMessage)
        assertNotNull(response, "Response tidak boleh null");
        assertEquals(TITLE_JAVA, response.title(), "Title harus sesuai dengan request");
        assertEquals(CATEGORY_EDUKASI, response.categoryName(), "Kategori harus sesuai dengan request");
        verify(readingTextRepository, times(1)).save(any(ReadingText.class));
    }

    @Test
    void createText_WhenRoleIsStudent_ShouldThrowException() {
        ReadingTextRequest request = new ReadingTextRequest(TITLE_JAVA, CONTENT_DUMMY, 1L);

        assertThrows(RuntimeException.class, () -> {
            readingTextService.createText(request, ROLE_STUDENT);
        }, "Harus melempar RuntimeException jika role bukan ADMIN");

        verify(readingTextRepository, never()).save(any(ReadingText.class));
    }

    @Test
    void getAllTexts_ShouldReturnListOfResponses() {
        Category category = new Category(1L, CATEGORY_EDUKASI);
        ReadingText text1 = new ReadingText(1L, "Judul 1", "Isi 1", category);
        ReadingText text2 = new ReadingText(2L, "Judul 2", "Isi 2", category);

        when(readingTextRepository.findAll()).thenReturn(List.of(text1, text2));

        List<ReadingTextResponse> responses = readingTextService.getAllTexts();

        assertNotNull(responses, "Daftar response tidak boleh null");
        assertEquals(2, responses.size(), "Jumlah item harus 2");
        assertEquals("Judul 1", responses.get(0).title(), "Judul item pertama harus sesuai");
        assertEquals(CATEGORY_EDUKASI, responses.get(0).categoryName(), "Kategori item pertama harus sesuai");
        verify(readingTextRepository, times(1)).findAll();
    }

    @Test
    void deleteText_WhenRoleIsAdmin_ShouldDeleteSuccessfully() {
        Long textId = 1L;
        when(readingTextRepository.existsById(textId)).thenReturn(true);

        assertDoesNotThrow(() -> readingTextService.deleteText(textId, ROLE_ADMIN),
                "Tidak boleh melempar exception saat dihapus oleh ADMIN");

        verify(readingTextRepository, times(1)).deleteById(textId);
    }

    @Test
    void deleteText_WhenRoleIsStudent_ShouldThrowException() {
        Long textId = 1L;

        assertThrows(RuntimeException.class,
                () -> readingTextService.deleteText(textId, ROLE_STUDENT),
                "Harus melempar RuntimeException jika role bukan ADMIN saat menghapus");

        verify(readingTextRepository, never()).deleteById(anyLong());
    }
}