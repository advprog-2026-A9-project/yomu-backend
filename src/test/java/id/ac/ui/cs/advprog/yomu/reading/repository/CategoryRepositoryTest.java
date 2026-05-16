package id.ac.ui.cs.advprog.yomu.reading.repository;

import id.ac.ui.cs.advprog.yomu.reading.model.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void testSaveAndFindCategory() {
        Category category = new Category();
        category.setName("Sejarah");

        Category savedCategory = categoryRepository.save(category);
        assertNotNull(savedCategory.getId());

        Optional<Category> found = categoryRepository.findById(savedCategory.getId());
        assertTrue(found.isPresent());
        assertEquals("Sejarah", found.get().getName());
    }
}