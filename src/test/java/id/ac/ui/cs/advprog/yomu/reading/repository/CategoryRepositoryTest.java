package id.ac.ui.cs.advprog.yomu.reading.repository;

import id.ac.ui.cs.advprog.yomu.reading.model.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts") // PMD Fix: Mengizinkan lebih dari 1 assert
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void testSaveAndFindCategory() {
        Category category = new Category();
        category.setName("Sejarah");

        Category savedCategory = categoryRepository.save(category);
        // PMD Fix: Menambahkan pesan penjelasan pada assert
        assertNotNull(savedCategory.getId(), "ID kategori yang baru disimpan tidak boleh null");

        Optional<Category> found = categoryRepository.findById(savedCategory.getId());
        // PMD Fix: Menambahkan pesan penjelasan pada assert
        assertTrue(found.isPresent(), "Kategori seharusnya dapat ditemukan di database");
        assertEquals("Sejarah", found.get().getName(), "Nama kategori yang ditemukan harus sesuai");
    }
}