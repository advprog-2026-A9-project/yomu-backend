package id.ac.ui.cs.advprog.yomu.auth.config;

import id.ac.ui.cs.advprog.yomu.auth.service.CustomOAuth2UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;


    @Test
    void publicEndpointShouldBeAccessible() throws Exception {
        final var result = mockMvc.perform(post("/api/auth/login"))
            .andExpect(status().is4xxClientError())
            .andReturn();
        assertNotNull(result, "Response should not be null");
    }

    @Test
    void protectedEndpointShouldReturn3xxWhenNotAuthenticated() throws Exception {
        final var result = mockMvc.perform(get("/api/readings"))
            .andExpect(status().is3xxRedirection())
            .andReturn();
        assertNotNull(result, "Response should not be null");
    }

    @Test
    void oauth2LoginEndpointShouldBeAccessible() throws Exception {
        final var result = mockMvc.perform(get("/oauth2/authorization/google"))
            .andExpect(status().is3xxRedirection())
            .andReturn();
        assertNotNull(result, "Response should not be null");
    }
}