package id.ac.ui.cs.advprog.yomu.reading.service;

import id.ac.ui.cs.advprog.yomu.reading.dto.ReadingTextRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.ReadingTextResponse;
import id.ac.ui.cs.advprog.yomu.reading.model.Category;
import id.ac.ui.cs.advprog.yomu.reading.model.ReadingText;
import id.ac.ui.cs.advprog.yomu.reading.repository.CategoryRepository;
import id.ac.ui.cs.advprog.yomu.reading.repository.ReadingTextRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
class ReadingTextServiceTest {

    private static final String TITLE_JAVA = "Belajar Java";
    private static final String CONTENT_DUMMY = "Isi teks tentang Java...";
    private static final String CATEGORY_EDUKASI = "Edukasi";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_PELAJAR = "PELAJAR";

    @Mock
    private ReadingTextRepository readingTextRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ReadingTextServiceImpl readingTextService;

    private ReadingTextRequest validRequest;
    private Category validCategory;
    private ReadingText savedText;

    @BeforeEach
    void setUp() {
        // Setup objek standar agar tidak mengulang kode di setiap blok Test
        validRequest = new ReadingTextRequest(TITLE_JAVA, CONTENT_DUMMY, 1L);
        validCategory = new Category(1L, CATEGORY_EDUKASI);
        savedText = new ReadingText(1L, TITLE_JAVA, CONTENT_DUMMY, validCategory);
    }

    // ==========================================
    // TEST CREATE TEXT (TDD: Relasi Category)
    // ==========================================

    @Test
    void createText_WhenRoleIsAdminAndCategoryExists_ShouldReturnSavedText() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(validCategory));
        when(readingTextRepository.save(any(ReadingText.class))).thenReturn(savedText);

        ReadingTextResponse response = readingTextService.createText(validRequest, ROLE_ADMIN);

        assertNotNull(response, "Response tidak boleh null");
        assertEquals(TITLE_JAVA, response.title(), "Title harus sesuai dengan request");
        assertEquals(CATEGORY_EDUKASI, response.categoryName(), "Kategori harus diambil dari Category DB");

        verify(categoryRepository, times(1)).findById(1L);
        verify(readingTextRepository, times(1)).save(any(ReadingText.class));
    }

    @Test
    void createText_WhenRoleIsAdminButCategoryNotFound_ShouldThrowException() {
        // Skenario penting: Admin kirim request tapi categoryId-nya ngawur
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(
                RuntimeException.class,
                () -> readingTextService.createText(validRequest, ROLE_ADMIN),
                "Harus melempar RuntimeException jika kategori tidak ditemukan"
        );

        assertTrue(exception.getMessage().toLowerCase().contains("category"), "Pesan error harus menyebutkan isu kategory");
        verify(readingTextRepository, never()).save(any(ReadingText.class));
    }

    @Test
    void createText_WhenRoleIsPelajar_ShouldThrowException() {
        assertThrows(
                RuntimeException.class,
                () -> readingTextService.createText(validRequest, ROLE_PELAJAR),
                "Harus melempar RuntimeException jika role bukan ADMIN"
        );

        verify(categoryRepository, never()).findById(anyLong());
        verify(readingTextRepository, never()).save(any(ReadingText.class));
    }

    // ==========================================
    // TEST GET ALL & GET BY ID
    // ==========================================

    @Test
    void getAllTexts_ShouldReturnListOfResponses() {
        when(readingTextRepository.findAll()).thenReturn(List.of(savedText));

        List<ReadingTextResponse> responses = readingTextService.getAllTexts();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(TITLE_JAVA, responses.get(0).title());
        assertEquals(CATEGORY_EDUKASI, responses.get(0).categoryName());
        verify(readingTextRepository, times(1)).findAll();
    }

    @Test
    void getTextById_WhenTextExists_ShouldReturnResponse() {
        when(readingTextRepository.findById(1L)).thenReturn(Optional.of(savedText));

        ReadingTextResponse response = readingTextService.getTextById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(TITLE_JAVA, response.title());
        verify(readingTextRepository, times(1)).findById(1L);
    }

    @Test
    void getTextById_WhenTextDoesNotExist_ShouldThrowException() {
        when(readingTextRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> readingTextService.getTextById(99L),
                "Harus melempar exception jika bacaan tidak ditemukan"
        );
    }

    // ==========================================
    // TEST DELETE TEXT
    // ==========================================

    @Test
    void deleteText_WhenRoleIsAdminAndTextExists_ShouldDeleteSuccessfully() {
        when(readingTextRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(
                () -> readingTextService.deleteText(1L, ROLE_ADMIN),
                "Tidak boleh melempar exception saat dihapus oleh ADMIN"
        );

        verify(readingTextRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteText_WhenRoleIsAdminButTextDoesNotExist_ShouldThrowException() {
        when(readingTextRepository.existsById(99L)).thenReturn(false);

        assertThrows(
                RuntimeException.class,
                () -> readingTextService.deleteText(99L, ROLE_ADMIN),
                "Harus melempar exception jika text yang mau dihapus tidak ada"
        );

        verify(readingTextRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteText_WhenRoleIsPelajar_ShouldThrowException() {
        assertThrows(
                RuntimeException.class,
                () -> readingTextService.deleteText(1L, ROLE_PELAJAR),
                "Harus melempar RuntimeException jika role bukan ADMIN saat menghapus"
        );

        verify(readingTextRepository, never()).deleteById(anyLong());
    }
}