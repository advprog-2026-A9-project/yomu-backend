package id.ac.ui.cs.advprog.yomu.reading.repository;
import id.ac.ui.cs.advprog.yomu.reading.model.QuizOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizOptionRepository extends JpaRepository<QuizOption, Long> {
}