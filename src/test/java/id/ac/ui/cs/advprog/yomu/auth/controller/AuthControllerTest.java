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
    private static final String ROLE_PELAJAR = "PELAJAR";
    private static final String ACCOUNT_URL = "/api/auth/account";
    private static final String RESPONSE_NOT_NULL = "Response should not be null";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    @WithMockUser(username = TEST_USER, roles = ROLE_PELAJAR)
    void getMeShouldReturnUserInfo() throws Exception {
        when(authService.getMe(TEST_USER)).thenReturn(
            new AuthResponse("123", TEST_USER, ROLE_PELAJAR, null, "OK")
        );

        final var result = mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value(TEST_USER))
            .andExpect(jsonPath("$.role").value(ROLE_PELAJAR))
            .andReturn();
        assertNotNull(result, RESPONSE_NOT_NULL);
    }

    @Test
    void getMeShouldReturn401WhenNotAuthenticated() throws Exception {
        final var result = mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized())
            .andReturn();
        assertNotNull(result, RESPONSE_NOT_NULL);
    }

    @Test
    @WithMockUser(username = TEST_USER, roles = ROLE_PELAJAR)
    void updateAccountShouldReturn200() throws Exception {
        when(authService.updateAccount(any(), any())).thenReturn(
            new AccountResponse("123", TEST_USER, "Mizuki", null, null, ROLE_PELAJAR, "Akun berhasil diperbarui")
        );

        final var result = mockMvc.perform(put(ACCOUNT_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"newusername\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Akun berhasil diperbarui"))
            .andReturn();
        assertNotNull(result, RESPONSE_NOT_NULL);
    }

    @Test
    void updateAccountShouldReturn401WhenNotAuthenticated() throws Exception {
        final var result = mockMvc.perform(put(ACCOUNT_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"newusername\"}"))
            .andExpect(status().isUnauthorized())
            .andReturn();
        assertNotNull(result, RESPONSE_NOT_NULL);
    }

    @Test
    @WithMockUser(username = TEST_USER, roles = ROLE_PELAJAR)
    void deleteAccountShouldReturn200() throws Exception {
        final var result = mockMvc.perform(delete(ACCOUNT_URL))
            .andExpect(status().isOk())
            .andReturn();
        assertNotNull(result, RESPONSE_NOT_NULL);
    }
    
    @Test
    void deleteAccountShouldReturn401WhenNotAuthenticated() throws Exception {
        final var result = mockMvc.perform(delete(ACCOUNT_URL))
            .andExpect(status().isUnauthorized())
            .andReturn();
        assertNotNull(result, RESPONSE_NOT_NULL);
    }

    @Test
    @WithMockUser(username = TEST_USER, roles = ROLE_PELAJAR)
    void linkLoginMethodShouldReturn200() throws Exception {
        when(authService.linkLoginMethod(any(), any())).thenReturn(
            new AccountResponse("123", TEST_USER, "Mizuki", "new@test.com", null, ROLE_PELAJAR, "Metode login berhasil ditautkan")
        );

        final var result = mockMvc.perform(post("/api/auth/account/link")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"new@test.com\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Metode login berhasil ditautkan"))
            .andReturn();
        assertNotNull(result, RESPONSE_NOT_NULL);
    }

    @Test
    void linkLoginMethodShouldReturn401WhenNotAuthenticated() throws Exception {
        final var result = mockMvc.perform(post("/api/auth/account/link")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"new@test.com\"}"))
            .andExpect(status().isUnauthorized())
            .andReturn();
        assertNotNull(result, RESPONSE_NOT_NULL);
    }

    @Test
    @WithMockUser(username = TEST_USER, roles = ROLE_PELAJAR)
    void logoutShouldReturn200() throws Exception {
        final var result = mockMvc.perform(post("/api/auth/logout"))
            .andExpect(status().isOk())
            .andReturn();
        assertNotNull(result, RESPONSE_NOT_NULL);
    }

    @Test
    void logoutShouldReturn401WhenNotAuthenticated() throws Exception {
        final var result = mockMvc.perform(post("/api/auth/logout"))
            .andExpect(status().isUnauthorized())
            .andReturn();
        assertNotNull(result, RESPONSE_NOT_NULL);
    }
}