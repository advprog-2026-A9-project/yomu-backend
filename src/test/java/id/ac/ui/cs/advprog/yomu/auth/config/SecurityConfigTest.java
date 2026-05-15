package id.ac.ui.cs.advprog.yomu.auth.config;

import id.ac.ui.cs.advprog.yomu.auth.service.CustomOAuth2UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    private static final String RESPONSE_NOT_NULL = "Response should not be null";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;


    @Test
    void publicEndpointShouldBeAccessible() throws Exception {
        final var result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identifier\":\"missing-user\",\"password\":\"secret\"}"))
            .andExpect(status().is4xxClientError())
            .andReturn();
        assertNotNull(result, RESPONSE_NOT_NULL);
    }

    @Test
    void protectedEndpointShouldReturn401WhenNotAuthenticated() throws Exception {
        final var result = mockMvc.perform(get("/api/readings"))
            .andExpect(status().isUnauthorized())
            .andReturn();
        assertNotNull(result, RESPONSE_NOT_NULL);
    }

    @Test
    void oauth2LoginEndpointShouldBeAccessible() throws Exception {
        final var result = mockMvc.perform(get("/oauth2/authorization/google"))
            .andExpect(status().is3xxRedirection())
            .andReturn();
        assertNotNull(result, RESPONSE_NOT_NULL);
    }

    @Test
    void oauth2PreflightShouldBeAllowedForFrontendOrigin() throws Exception {
        final var result = mockMvc.perform(options("/oauth2/authorization/google")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
            .andReturn();
        assertNotNull(result, RESPONSE_NOT_NULL);
    }
}