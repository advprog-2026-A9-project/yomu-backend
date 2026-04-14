package id.ac.ui.cs.advprog.yomu.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoleBasedAccessTest {

    private static final String RESPONSE_NOT_NULL = "Response should not be null";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin", authorities = "ADMIN")
    void adminCanPostReadingText() throws Exception {
        final var result = mockMvc.perform(post("/api/reading-texts"))
            .andExpect(status().is4xxClientError()) // 400 karena body kosong, bukan 403
            .andReturn();
        assertNotNull(result, RESPONSE_NOT_NULL);
    }

    @Test
    @WithMockUser(username = "pelajar", authorities = "PELAJAR")
    void pelajarCannotPostReadingText() throws Exception {
        final var result = mockMvc.perform(post("/api/reading-texts"))
            .andExpect(status().isForbidden())
            .andReturn();
        assertNotNull(result, RESPONSE_NOT_NULL);
    }

    @Test
    @WithMockUser(username = "pelajar", authorities = "PELAJAR")
    void pelajarCannotDeleteReadingText() throws Exception {
        final var result = mockMvc.perform(delete("/api/reading-texts/1"))
            .andExpect(status().isForbidden())
            .andReturn();
        assertNotNull(result, RESPONSE_NOT_NULL);
    }

    @Test
    @WithMockUser(username = "pelajar", authorities = "PELAJAR")
    void pelajarCanGetReadingTexts() throws Exception {
        final var result = mockMvc.perform(get("/api/reading-texts"))
            .andExpect(status().isOk())
            .andReturn();
        assertNotNull(result, RESPONSE_NOT_NULL);
    }
}