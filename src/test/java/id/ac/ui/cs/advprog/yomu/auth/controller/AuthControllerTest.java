package id.ac.ui.cs.advprog.yomu.auth.controller;

import id.ac.ui.cs.advprog.yomu.auth.dto.AuthResponse;
import id.ac.ui.cs.advprog.yomu.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    private static final String TEST_USER = "testuser";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    @WithMockUser(username = TEST_USER, roles = "PELAJAR")
    void getMeShouldReturnUserInfo() throws Exception {
        when(authService.getMe(TEST_USER)).thenReturn(
            new AuthResponse("123", TEST_USER, "PELAJAR", null, "OK")
        );

        final var result = mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value(TEST_USER))
            .andExpect(jsonPath("$.role").value("PELAJAR"))
            .andReturn();
        assertNotNull(result, "Response should not be null");
    }

    @Test
    void getMeShouldReturn401WhenNotAuthenticated() throws Exception {
        final var result = mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized())
            .andReturn();
        assertNotNull(result, "Response should not be null");
    }
}