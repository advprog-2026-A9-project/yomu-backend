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
import java.util.Locale;
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
        validRequest = new ReadingTextRequest(TITLE_JAVA, CONTENT_DUMMY, 1L);
        validCategory = new Category(1L, CATEGORY_EDUKASI);
        savedText = new ReadingText(1L, TITLE_JAVA, CONTENT_DUMMY, validCategory);
    }

    @Test
    void createText_WhenCategoryExists_ShouldReturnSavedText() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(validCategory));
        when(readingTextRepository.save(any(ReadingText.class))).thenReturn(savedText);

        ReadingTextResponse response = readingTextService.createText(validRequest);

        assertNotNull(response, "Response tidak boleh null");
        assertEquals(TITLE_JAVA, response.title(), "Title harus sesuai dengan request");
        assertEquals(CATEGORY_EDUKASI, response.categoryName(), "Kategori harus diambil dari Category DB");

        verify(categoryRepository, times(1)).findById(1L);
        verify(readingTextRepository, times(1)).save(any(ReadingText.class));
    }

    @Test
    void createText_WhenCategoryNotFound_ShouldThrowException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(
                RuntimeException.class,
                () -> readingTextService.createText(validRequest),
                "Harus melempar RuntimeException jika kategori tidak ditemukan"
        );

        assertTrue(exception.getMessage().toLowerCase(Locale.ROOT).contains("category"), "Pesan error harus menyebutkan isu kategory");
        verify(readingTextRepository, never()).save(any(ReadingText.class));
    }

    @Test
    void updateText_WhenTextAndCategoryExist_ShouldReturnUpdatedText() {
        when(readingTextRepository.findById(1L)).thenReturn(Optional.of(savedText));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(validCategory));
        when(readingTextRepository.save(any(ReadingText.class))).thenReturn(savedText);

        ReadingTextResponse response = readingTextService.updateText(1L, validRequest);

        assertNotNull(response, "Response tidak boleh null");
        verify(readingTextRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).findById(1L);
        verify(readingTextRepository, times(1)).save(any(ReadingText.class));
    }

    @Test
    void updateText_WhenTextNotFound_ShouldThrowException() {
        when(readingTextRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> readingTextService.updateText(99L, validRequest),
                "Harus melempar exception jika text tidak ditemukan"
        );
        verify(categoryRepository, never()).findById(anyLong());
        verify(readingTextRepository, never()).save(any(ReadingText.class));
    }

    @Test
    void updateText_WhenCategoryNotFound_ShouldThrowException() {
        when(readingTextRepository.findById(1L)).thenReturn(Optional.of(savedText));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> readingTextService.updateText(1L, validRequest),
                "Harus melempar exception jika kategori tidak ditemukan"
        );
        verify(readingTextRepository, never()).save(any(ReadingText.class));
    }

    @Test
    void getAllTexts_ShouldReturnListOfResponses() {
        when(readingTextRepository.findAll()).thenReturn(List.of(savedText));

        List<ReadingTextResponse> responses = readingTextService.getAllTexts();

        assertNotNull(responses, "Daftar respons tidak boleh null");
        assertEquals(1, responses.size(), "Ukuran daftar respons harus 1");
        verify(readingTextRepository, times(1)).findAll();
    }

    @Test
    void getTextById_WhenTextExists_ShouldReturnResponse() {
        when(readingTextRepository.findById(1L)).thenReturn(Optional.of(savedText));

        ReadingTextResponse response = readingTextService.getTextById(1L);

        assertNotNull(response, "Respons tidak boleh null jika text ditemukan");
        assertEquals(1L, response.id(), "ID respons harus sama dengan ID yang diminta");
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

    @Test
    void deleteText_WhenTextExists_ShouldDeleteSuccessfully() {
        when(readingTextRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(
                () -> readingTextService.deleteText(1L),
                "Tidak boleh melempar exception saat dihapus"
        );

        verify(readingTextRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteText_WhenTextDoesNotExist_ShouldThrowException() {
        when(readingTextRepository.existsById(99L)).thenReturn(false);

        assertThrows(
                RuntimeException.class,
                () -> readingTextService.deleteText(99L),
                "Harus melempar exception jika text yang mau dihapus tidak ada"
        );

        verify(readingTextRepository, never()).deleteById(anyLong());
    }
}