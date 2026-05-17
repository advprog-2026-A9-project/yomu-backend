package id.ac.ui.cs.advprog.yomu.reading.service;

import id.ac.ui.cs.advprog.yomu.reading.model.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    Category getCategoryById(Long id);
    Category createCategory(String name);
    void deleteCategory(Long id);
}