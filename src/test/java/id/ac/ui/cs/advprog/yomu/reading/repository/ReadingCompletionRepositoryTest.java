package id.ac.ui.cs.advprog.yomu.reading.repository;

import id.ac.ui.cs.advprog.yomu.reading.model.Category;
import id.ac.ui.cs.advprog.yomu.reading.model.ReadingCompletion;
import id.ac.ui.cs.advprog.yomu.reading.model.ReadingText;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts") // PMD Fix: Mengizinkan lebih dari 1 assert dalam 1 test
class ReadingCompletionRepositoryTest {

    @Autowired
    private ReadingCompletionRepository completionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testExistsByUserIdAndReadingTextId() {
        Category category = new Category();
        category.setName("Sastra");
        category = entityManager.persistAndFlush(category);

        ReadingText text = new ReadingText();
        text.setTitle("Puisi");
        text.setContent("Isi");
        text.setCategory(category);
        text = entityManager.persistAndFlush(text);

        ReadingCompletion completion = new ReadingCompletion();
        completion.setUserId("user-123");
        completion.setReadingText(text);
        completion.setScore(100);
        completion.setCorrectAnswers(5);
        completion.setTotalQuestions(5);

        completionRepository.save(completion);

        // Test custom method kita
        boolean exists = completionRepository.existsByUserIdAndReadingTextId("user-123", text.getId());
        boolean notExists = completionRepository.existsByUserIdAndReadingTextId("user-999", text.getId());

        // PMD Fix: Menambahkan pesan pada semua assert
        assertTrue(exists, "Reading completion seharusnya ditemukan untuk user yang sudah menyelesaikan");
        assertFalse(notExists, "Reading completion tidak boleh ditemukan untuk user yang belum mengerjakan");
    }
}