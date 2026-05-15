package id.ac.ui.cs.advprog.yomu.reading.repository;

import id.ac.ui.cs.advprog.yomu.reading.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Kerangka Repository sementara agar Mockito bisa nge-mock
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}