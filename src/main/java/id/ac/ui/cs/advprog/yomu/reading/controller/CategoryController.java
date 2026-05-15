package id.ac.ui.cs.advprog.yomu.reading.controller;

import id.ac.ui.cs.advprog.yomu.reading.model.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    // Return null agar Test gagal (RED)

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return null;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return null;
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Map<String, String> request, Authentication authentication) {
        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id, Authentication authentication) {
        return null;
    }
}