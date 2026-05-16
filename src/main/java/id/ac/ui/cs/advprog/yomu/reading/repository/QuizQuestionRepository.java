package id.ac.ui.cs.advprog.yomu.reading.repository;
import id.ac.ui.cs.advprog.yomu.reading.model.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByReadingTextId(Long readingTextId);
}