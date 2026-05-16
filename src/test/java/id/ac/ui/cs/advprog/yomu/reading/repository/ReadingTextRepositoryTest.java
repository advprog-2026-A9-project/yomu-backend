package id.ac.ui.cs.advprog.yomu.reading.repository;

import id.ac.ui.cs.advprog.yomu.reading.model.Category;
import id.ac.ui.cs.advprog.yomu.reading.model.ReadingText;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ReadingTextRepositoryTest {

    @Autowired
    private ReadingTextRepository readingTextRepository;

    @Autowired
    private TestEntityManager entityManager; // Digunakan untuk persist data relasi sebelum dites

    @Test
    void testSaveAndFindReadingText() {
        // Setup Kategori dulu karena ReadingText butuh Kategori (ManyToOne)
        Category category = new Category();
        category.setName("Sains");
        category = entityManager.persistAndFlush(category);

        ReadingText text = new ReadingText();
        text.setTitle("Fisika Quantum");
        text.setContent("Isi materi fisika...");
        text.setCategory(category);

        ReadingText savedText = readingTextRepository.save(text);

        assertNotNull(savedText.getId());
        assertEquals("Fisika Quantum", savedText.getTitle());
        assertEquals("Sains", savedText.getCategory().getName());
    }
}