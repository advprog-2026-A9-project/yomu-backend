package id.ac.ui.cs.advprog.yomu.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicEndpointShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/auth/login"))
            .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointShouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/readings"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void oauth2LoginEndpointShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google"))
            .andExpect(status().is3xxRedirection());
    }
}