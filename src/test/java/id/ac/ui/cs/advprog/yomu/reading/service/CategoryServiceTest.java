package id.ac.ui.cs.advprog.yomu.reading.service;

import id.ac.ui.cs.advprog.yomu.reading.model.Category;
import id.ac.ui.cs.advprog.yomu.reading.repository.CategoryRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
class CategoryServiceTest {

    private static final String CAT_NAME = "Teknologi";

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category(1L, CAT_NAME);
    }

    // ==========================================
    // TEST GET ALL CATEGORIES
    // ==========================================

    @Test
    void getAllCategories_ShouldReturnList() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<Category> result = categoryService.getAllCategories();

        assertNotNull(result, "Daftar kategori tidak boleh null");
        assertEquals(1, result.size(), "Jumlah kategori harus 1");
        assertEquals(CAT_NAME, result.get(0).getName(), "Nama kategori harus sesuai");
        verify(categoryRepository, times(1)).findAll();
    }

    // ==========================================
    // TEST CREATE CATEGORY
    // ==========================================

    @Test
    void createCategory_ShouldSave() {
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category result = categoryService.createCategory(CAT_NAME);

        assertNotNull(result, "Kategori yang dibuat tidak boleh null");
        assertEquals(CAT_NAME, result.getName(), "Nama kategori yang dibuat harus sesuai");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }


    // ==========================================
    // TEST GET BY ID
    // ==========================================

    @Test
    void getCategoryById_WhenExists_ShouldReturnCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category result = categoryService.getCategoryById(1L);

        assertNotNull(result, "Kategori tidak boleh null jika ditemukan");
        assertEquals(1L, result.getId(), "ID kategori harus cocok dengan yang dicari");
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void getCategoryById_WhenDoesNotExist_ShouldThrowException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> categoryService.getCategoryById(99L),
                "Harus melempar exception jika kategori tidak ditemukan"
        );
    }

    // ==========================================
    // TEST DELETE CATEGORY
    // ==========================================

    @Test
    void deleteCategory_WhenExists_ShouldDelete() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(
                () -> categoryService.deleteCategory(1L),
                "Penghapusan kategori tanpa exception"
        );

        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCategory_WhenNotFound_ShouldThrowException() {
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertThrows(
                RuntimeException.class,
                () -> categoryService.deleteCategory(99L),
                "Harus melempar exception jika kategori yang mau dihapus tidak ada"
        );
        verify(categoryRepository, never()).deleteById(anyLong());
    }
}