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

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_PELAJAR = "PELAJAR";
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

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(CAT_NAME, result.get(0).getName());
        verify(categoryRepository, times(1)).findAll();
    }

    // ==========================================
    // TEST CREATE CATEGORY
    // ==========================================

    @Test
    void createCategory_WhenRoleIsAdmin_ShouldSave() {
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category result = categoryService.createCategory(CAT_NAME, ROLE_ADMIN);

        assertNotNull(result);
        assertEquals(CAT_NAME, result.getName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_WhenRoleIsPelajar_ShouldThrowException() {
        assertThrows(
                RuntimeException.class,
                () -> categoryService.createCategory(CAT_NAME, ROLE_PELAJAR),
                "Harus melempar exception jika Pelajar mencoba membuat kategori"
        );
        verify(categoryRepository, never()).save(any(Category.class));
    }

    // ==========================================
    // TEST GET BY ID
    // ==========================================

    @Test
    void getCategoryById_WhenExists_ShouldReturnCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category result = categoryService.getCategoryById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void getCategoryById_WhenDoesNotExist_ShouldThrowException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> categoryService.getCategoryById(99L)
        );
    }

    // ==========================================
    // TEST DELETE CATEGORY
    // ==========================================

    @Test
    void deleteCategory_WhenRoleIsAdminAndExists_ShouldDelete() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> categoryService.deleteCategory(1L, ROLE_ADMIN));

        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCategory_WhenRoleIsAdminButNotFound_ShouldThrowException() {
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertThrows(
                RuntimeException.class,
                () -> categoryService.deleteCategory(99L, ROLE_ADMIN)
        );
        verify(categoryRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteCategory_WhenRoleIsPelajar_ShouldThrowException() {
        assertThrows(
                RuntimeException.class,
                () -> categoryService.deleteCategory(1L, ROLE_PELAJAR)
        );
        verify(categoryRepository, never()).deleteById(anyLong());
    }
}