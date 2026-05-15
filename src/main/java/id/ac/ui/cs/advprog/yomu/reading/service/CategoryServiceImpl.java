package id.ac.ui.cs.advprog.yomu.reading.service;

import id.ac.ui.cs.advprog.yomu.reading.model.Category;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    // Semua me-return null agar Test dipastikan gagal (RED)

    @Override
    public List<Category> getAllCategories() {
        return null;
    }

    @Override
    public Category getCategoryById(Long id) {
        return null;
    }

    @Override
    public Category createCategory(String name, String role) {
        return null;
    }

    @Override
    public void deleteCategory(Long id, String role) {
        // Kosong
    }
}