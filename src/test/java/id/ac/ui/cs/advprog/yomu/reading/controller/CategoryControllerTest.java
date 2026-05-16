package id.ac.ui.cs.advprog.yomu.reading.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import id.ac.ui.cs.advprog.yomu.reading.model.Category;
import id.ac.ui.cs.advprog.yomu.reading.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
class CategoryControllerTest {

    private static final Long CATEGORY_ID = 1L;
    private static final String CATEGORY_NAME = "Teknologi";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private Category validCategory;

    @BeforeEach
    void setUp() {
        // Setup objek kategori simulasi
        validCategory = new Category(CATEGORY_ID, CATEGORY_NAME);
    }

    // ==========================================
    // TEST GET ALL CATEGORIES (GET)
    // ==========================================

    @Test
    @WithMockUser
    void getAllCategories_ShouldReturnOk() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(validCategory));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(CATEGORY_ID))
                .andExpect(jsonPath("$[0].name").value(CATEGORY_NAME));

        verify(categoryService, times(1)).getAllCategories();
    }

    // ==========================================
    // TEST GET BY ID (GET)
    // ==========================================

    @Test
    @WithMockUser
    void getCategoryById_ShouldReturnOk() throws Exception {
        when(categoryService.getCategoryById(CATEGORY_ID)).thenReturn(validCategory);

        mockMvc.perform(get("/api/categories/{id}", CATEGORY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CATEGORY_ID))
                .andExpect(jsonPath("$.name").value(CATEGORY_NAME));

        verify(categoryService, times(1)).getCategoryById(CATEGORY_ID);
    }

    // ==========================================
    // TEST CREATE CATEGORY (POST)
    // ==========================================

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void createCategory_WhenAuthorized_ShouldReturnCreated() throws Exception {
        // Menggunakan Map untuk mensimulasikan JSON request body: {"name": "Teknologi"}
        Map<String, String> requestBody = Map.of("name", CATEGORY_NAME);

        when(categoryService.createCategory(eq(CATEGORY_NAME), anyString())).thenReturn(validCategory);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(CATEGORY_ID))
                .andExpect(jsonPath("$.name").value(CATEGORY_NAME));

        verify(categoryService, times(1)).createCategory(eq(CATEGORY_NAME), anyString());
    }

    // ==========================================
    // TEST DELETE CATEGORY (DELETE)
    // ==========================================

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void deleteCategory_WhenAuthorized_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/categories/{id}", CATEGORY_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(categoryService, times(1)).deleteCategory(eq(CATEGORY_ID), anyString());
    }
}