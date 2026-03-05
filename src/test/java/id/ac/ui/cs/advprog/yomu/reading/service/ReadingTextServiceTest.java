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
class ReadingTextServiceTest {

    @Mock
    private ReadingTextRepository readingTextRepository;

    @Mock
    private CategoryRepository categoryRepository;

    // INI AKAN MERAH: Karena class-nya belum kita buat
    @InjectMocks
    private ReadingTextServiceImpl readingTextService;

    @Test
    void createText_WhenRoleIsAdmin_ShouldReturnSavedText() {
        // Arrange
        String role = "ADMIN";
        ReadingTextRequest request = new ReadingTextRequest("Belajar Java", "Isi teks...", 1L);
        Category category = new Category(1L, "Edukasi");
        ReadingText savedText = new ReadingText(1L, "Belajar Java", "Isi teks...", category);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(readingTextRepository.save(any(ReadingText.class))).thenReturn(savedText);

        // Act
        // INI JUGA MERAH: method createText belum ada
        ReadingTextResponse response = readingTextService.createText(request, role);

        // Assert
        assertNotNull(response);
        assertEquals("Belajar Java", response.title());
        assertEquals("Edukasi", response.categoryName());
        verify(readingTextRepository, times(1)).save(any(ReadingText.class));
    }

    @Test
    void createText_WhenRoleIsStudent_ShouldThrowException() {
        // Arrange
        String role = "STUDENT";
        ReadingTextRequest request = new ReadingTextRequest("Belajar Java", "Isi teks...", 1L);

        // Act & Assert
        // INI AKAN MERAH: Exception-nya belum ada
        assertThrows(RuntimeException.class, () -> {
            readingTextService.createText(request, role);
        });
        verify(readingTextRepository, never()).save(any(ReadingText.class));
    }

    @Test
    void getAllTexts_ShouldReturnListOfResponses() {
        // Arrange
        Category category = new Category(1L, "Edukasi");
        ReadingText text1 = new ReadingText(1L, "Judul 1", "Isi 1", category);
        ReadingText text2 = new ReadingText(2L, "Judul 2", "Isi 2", category);

        when(readingTextRepository.findAll()).thenReturn(List.of(text1, text2));

        // Act
        List<ReadingTextResponse> responses = readingTextService.getAllTexts();

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Judul 1", responses.get(0).title());
        assertEquals("Edukasi", responses.get(0).categoryName());
        verify(readingTextRepository, times(1)).findAll();
    }

    @Test
    void deleteText_WhenRoleIsAdmin_ShouldDeleteSuccessfully() {
        // Arrange
        String role = "ADMIN";
        Long textId = 1L;
        when(readingTextRepository.existsById(textId)).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> readingTextService.deleteText(textId, role));

        // Assert
        verify(readingTextRepository, times(1)).deleteById(textId);
    }

    @Test
    void deleteText_WhenRoleIsStudent_ShouldThrowException() {
        // Arrange
        String role = "STUDENT";
        Long textId = 1L;

        // Act & Assert
        assertThrows(RuntimeException.class, () -> readingTextService.deleteText(textId, role));

        // Pastikan repository.deleteById tidak pernah dipanggil
        verify(readingTextRepository, never()).deleteById(anyLong());
    }
}