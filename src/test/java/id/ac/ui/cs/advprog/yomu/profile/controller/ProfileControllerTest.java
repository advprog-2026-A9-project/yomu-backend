package id.ac.ui.cs.advprog.yomu.profile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.yomu.profile.dto.UpdateBioRequest;
import id.ac.ui.cs.advprog.yomu.profile.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProfileControllerTest {

    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_USERNAME = "prasetya";
    private static final String TEST_BIO = "New bio!";
    private static final String ERROR_MOCKMVC_NULL = "MockMvc instance must be autowired and not null";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProfileResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = ProfileResponse.builder()
                .userId(TEST_USER_ID)
                .username(TEST_USERNAME)
                .displayName("Prasetya")
                .bio(TEST_BIO)
                .joinedDate("Mei 2026")
                .readingStats(new ProfileResponse.ReadingStatsDto(5, 40, 85))
                .build();
    }

    @Test
    @WithMockUser(username = TEST_USERNAME)
    void getMyProfileShouldReturnProfile() throws Exception {
        when(profileService.getProfileByUserIdOrUsername(TEST_USERNAME)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/profile/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(TEST_USER_ID))
                .andExpect(jsonPath("$.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.bio").value(TEST_BIO));

        assertNotNull(mockMvc, ERROR_MOCKMVC_NULL);
    }

    @Test
    void getMyProfileUnauthorizedWhenNoUser() throws Exception {
        mockMvc.perform(get("/api/profile/me"))
                .andExpect(status().isUnauthorized());

        assertNotNull(mockMvc, ERROR_MOCKMVC_NULL);
    }

    @Test
    @WithMockUser(username = TEST_USERNAME)
    void getProfileByIdentifierShouldReturnProfile() throws Exception {
        when(profileService.getProfileByUserIdOrUsername(TEST_USERNAME)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/profile/prasetya"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(TEST_USER_ID))
                .andExpect(jsonPath("$.username").value(TEST_USERNAME));

        assertNotNull(mockMvc, ERROR_MOCKMVC_NULL);
    }

    @Test
    @WithMockUser(username = TEST_USERNAME)
    void updateBioShouldReturnUpdatedProfile() throws Exception {
        UpdateBioRequest request = new UpdateBioRequest(TEST_BIO);
        
        when(profileService.getProfileByUserIdOrUsername(TEST_USERNAME)).thenReturn(mockResponse);
        when(profileService.updateBio(TEST_USER_ID, TEST_BIO)).thenReturn(mockResponse);

        mockMvc.perform(put("/api/profile/bio")
                        .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(java.util.Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value(TEST_BIO));

        assertNotNull(mockMvc, ERROR_MOCKMVC_NULL);
    }

    @Test
    void updateBioUnauthorizedWhenNoUser() throws Exception {
        UpdateBioRequest request = new UpdateBioRequest(TEST_BIO);

        mockMvc.perform(put("/api/profile/bio")
                        .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(java.util.Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isUnauthorized());

        assertNotNull(mockMvc, ERROR_MOCKMVC_NULL);
    }
}
