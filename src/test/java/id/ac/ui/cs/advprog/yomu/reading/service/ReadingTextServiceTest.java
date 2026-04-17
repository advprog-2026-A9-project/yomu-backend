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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
class ReadingTextServiceTest {

    private static final String TITLE_JAVA = "Belajar Java";
    private static final String CONTENT_DUMMY = "Isi teks...";
    private static final String CATEGORY_EDUKASI = "Edukasi";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_PELAJAR = "PELAJAR";
    private static final String TITLE_ONE = "Judul 1";
    private static final String TITLE_TWO = "Judul 2";
    private static final String CONTENT_ONE = "Isi 1";
    private static final String CONTENT_TWO = "Isi 2";

    @Mock
    private ReadingTextRepository readingTextRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ReadingTextServiceImpl readingTextService;

    @Test
    void createText_WhenRoleIsAdmin_ShouldReturnSavedText() {
        final ReadingTextRequest request = new ReadingTextRequest(TITLE_JAVA, CONTENT_DUMMY, 1L);
        final Category category = new Category(1L, CATEGORY_EDUKASI);
        final ReadingText savedText = new ReadingText(1L, TITLE_JAVA, CONTENT_DUMMY, category);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(readingTextRepository.save(any(ReadingText.class))).thenReturn(savedText);

        final ReadingTextResponse response = readingTextService.createText(request, ROLE_ADMIN);

        assertNotNull(response, "Response tidak boleh null");
        assertEquals(TITLE_JAVA, response.title(), "Title harus sesuai dengan request");
        assertEquals(CATEGORY_EDUKASI, response.categoryName(), "Kategori harus sesuai dengan request");
        verify(readingTextRepository, times(1)).save(any(ReadingText.class));
    }

    @Test
    void createText_WhenRoleIsPelajar_ShouldThrowException() {
        final ReadingTextRequest request = new ReadingTextRequest(TITLE_JAVA, CONTENT_DUMMY, 1L);

        assertThrows(
                RuntimeException.class,
                () -> readingTextService.createText(request, ROLE_PELAJAR),
                "Harus melempar RuntimeException jika role bukan ADMIN"
        );

        verify(readingTextRepository, never()).save(any(ReadingText.class));
    }

    @Test
    void getAllTexts_ShouldReturnListOfResponses() {
        final Category category = new Category(1L, CATEGORY_EDUKASI);
        final ReadingText text1 = new ReadingText(1L, TITLE_ONE, CONTENT_ONE, category);
        final ReadingText text2 = new ReadingText(2L, TITLE_TWO, CONTENT_TWO, category);

        when(readingTextRepository.findAll()).thenReturn(List.of(text1, text2));

        final List<ReadingTextResponse> responses = readingTextService.getAllTexts();

        assertNotNull(responses, "Daftar response tidak boleh null");
        assertEquals(2, responses.size(), "Jumlah item harus 2");
        assertEquals(TITLE_ONE, responses.get(0).title(), "Judul item pertama harus sesuai");
        assertEquals(CATEGORY_EDUKASI, responses.get(0).categoryName(), "Kategori item pertama harus sesuai");
        verify(readingTextRepository, times(1)).findAll();
    }

    @Test
    void getTextById_WhenTextExists_ShouldReturnResponse() {
        final Category category = new Category(1L, CATEGORY_EDUKASI);
        final ReadingText text = new ReadingText(1L, TITLE_ONE, CONTENT_ONE, category);

        when(readingTextRepository.findById(1L)).thenReturn(Optional.of(text));

        final ReadingTextResponse response = readingTextService.getTextById(1L);

        assertNotNull(response, "Response tidak boleh null");
        assertEquals(1L, response.id(), "ID harus sesuai");
        assertEquals(TITLE_ONE, response.title(), "Judul harus sesuai");
        assertEquals(CONTENT_ONE, response.content(), "Konten harus sesuai");
        assertEquals(CATEGORY_EDUKASI, response.categoryName(), "Kategori harus sesuai");
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
    void deleteText_WhenRoleIsAdmin_ShouldDeleteSuccessfully() {
        final Long textId = 1L;
        when(readingTextRepository.existsById(textId)).thenReturn(true);

        assertDoesNotThrow(
                () -> readingTextService.deleteText(textId, ROLE_ADMIN),
                "Tidak boleh melempar exception saat dihapus oleh ADMIN"
        );

        verify(readingTextRepository, times(1)).deleteById(textId);
    }

    @Test
    void deleteText_WhenRoleIsPelajar_ShouldThrowException() {
        final Long textId = 1L;

        assertThrows(
                RuntimeException.class,
                () -> readingTextService.deleteText(textId, ROLE_PELAJAR),
                "Harus melempar RuntimeException jika role bukan ADMIN saat menghapus"
        );

        verify(readingTextRepository, never()).deleteById(anyLong());
    }
}