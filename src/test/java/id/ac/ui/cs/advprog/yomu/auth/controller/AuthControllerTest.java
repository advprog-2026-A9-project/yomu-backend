package id.ac.ui.cs.advprog.yomu.auth.controller;

import id.ac.ui.cs.advprog.yomu.auth.dto.AccountResponse;
import id.ac.ui.cs.advprog.yomu.auth.dto.AuthResponse;
import id.ac.ui.cs.advprog.yomu.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

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

    @Test
    @WithMockUser(username = TEST_USER, roles = "PELAJAR")
    void updateAccountShouldReturn200() throws Exception {
        when(authService.updateAccount(any(), any())).thenReturn(
            new AccountResponse("123", TEST_USER, "Mizuki", null, null, "PELAJAR", "Akun berhasil diperbarui")
        );

        mockMvc.perform(put("/api/auth/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"newusername\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Akun berhasil diperbarui"));
    }

    @Test
    void updateAccountShouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/api/auth/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"newusername\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = TEST_USER, roles = "PELAJAR")
    void deleteAccountShouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/auth/account"))
            .andExpect(status().isOk());
    }

    @Test
    void deleteAccountShouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/auth/account"))
            .andExpect(status().isUnauthorized());
    }

    // ====== LINK LOGIN METHOD ======

    @Test
    @WithMockUser(username = TEST_USER, roles = "PELAJAR")
    void linkLoginMethodShouldReturn200() throws Exception {
        when(authService.linkLoginMethod(any(), any())).thenReturn(
            new AccountResponse("123", TEST_USER, "Mizuki", "new@test.com", null, "PELAJAR", "Metode login berhasil ditautkan")
        );

        mockMvc.perform(post("/api/auth/account/link")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"new@test.com\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Metode login berhasil ditautkan"));
    }

    @Test
    void linkLoginMethodShouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/auth/account/link")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"new@test.com\"}"))
            .andExpect(status().isUnauthorized());
    }
}