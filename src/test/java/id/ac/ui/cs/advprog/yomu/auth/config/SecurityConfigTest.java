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

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;

    @Test
    void publicEndpointShouldBeAccessible() throws Exception {
        mockMvc.perform(post("/api/auth/login"))
            .andExpect(status().is4xxClientError()); // 400 karena body kosong, tapi endpoint accessible
    }

    @Test
    void protectedEndpointShouldReturn3xxWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/readings"))
            .andExpect(status().is3xxRedirection()); // redirect ke google login
    }

    @Test
    void oauth2LoginEndpointShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google"))
            .andExpect(status().is3xxRedirection());
    }
}