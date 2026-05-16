package id.ac.ui.cs.advprog.yomu.reading.repository;

import id.ac.ui.cs.advprog.yomu.reading.model.Category;
import id.ac.ui.cs.advprog.yomu.reading.model.QuizQuestion;
import id.ac.ui.cs.advprog.yomu.reading.model.ReadingText;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class QuizQuestionRepositoryTest {

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testFindByReadingTextId() {
        Category category = new Category();
        category.setName("Sains");
        category = entityManager.persistAndFlush(category);

        ReadingText text = new ReadingText();
        text.setTitle("Biologi");
        text.setContent("Isi");
        text.setCategory(category);
        text = entityManager.persistAndFlush(text);

        QuizQuestion q1 = new QuizQuestion();
        q1.setQuestionText("Apa itu sel?");
        q1.setReadingText(text);
        quizQuestionRepository.save(q1);

        QuizQuestion q2 = new QuizQuestion();
        q2.setQuestionText("Apa itu DNA?");
        q2.setReadingText(text);
        quizQuestionRepository.save(q2);

        // Test custom method kita
        List<QuizQuestion> questions = quizQuestionRepository.findByReadingTextId(text.getId());

        assertEquals(2, questions.size(), "Hidup Jo..");
    }
}