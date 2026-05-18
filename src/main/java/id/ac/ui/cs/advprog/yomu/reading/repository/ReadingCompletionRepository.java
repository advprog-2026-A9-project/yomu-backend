package id.ac.ui.cs.advprog.yomu.reading.repository;
import id.ac.ui.cs.advprog.yomu.reading.model.ReadingCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReadingCompletionRepository extends JpaRepository<ReadingCompletion, Long> {
    boolean existsByUserIdAndReadingTextId(String userId, Long readingTextId);
    Optional<ReadingCompletion> findByUserIdAndReadingTextId(String userId, Long readingTextId);
}