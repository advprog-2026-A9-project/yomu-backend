package id.ac.ui.cs.advprog.yomu.reading.repository;

import id.ac.ui.cs.advprog.yomu.reading.model.ReadingText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ReadingTextRepository extends JpaRepository<ReadingText, UUID> {
}