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
}