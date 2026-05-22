package id.ac.ui.cs.advprog.yomu.reading.repository;

import id.ac.ui.cs.advprog.yomu.reading.model.Category;
import id.ac.ui.cs.advprog.yomu.reading.model.QuizOption;
import id.ac.ui.cs.advprog.yomu.reading.model.QuizQuestion;
import id.ac.ui.cs.advprog.yomu.reading.model.ReadingText;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts") // PMD Fix: Mengizinkan lebih dari 1 assert
class QuizOptionRepositoryTest {

    @Autowired
    private QuizOptionRepository quizOptionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testSaveQuizOption() {
        Category category = new Category();
        category.setName("IT");
        category = entityManager.persistAndFlush(category);

        ReadingText text = new ReadingText();
        text.setTitle("Java");
        text.setContent("Isi");
        text.setCategory(category);
        text = entityManager.persistAndFlush(text);

        QuizQuestion question = new QuizQuestion();
        question.setQuestionText("Apa itu OOP?");
        question.setReadingText(text);
        question = entityManager.persistAndFlush(question);

        QuizOption option = new QuizOption();
        option.setOptionText("Object Oriented");
        option.setCorrect(true);
        option.setQuizQuestion(question);

        QuizOption savedOption = quizOptionRepository.save(option);

        // PMD Fix: Menambahkan pesan pada setiap assert
        assertNotNull(savedOption.getId(), "ID opsi kuis yang disimpan tidak boleh null");
        assertTrue(savedOption.isCorrect(), "Opsi kuis harus tersimpan dengan status kebenaran yang sesuai");
    }
}