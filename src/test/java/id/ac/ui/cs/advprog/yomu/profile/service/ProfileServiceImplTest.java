package id.ac.ui.cs.advprog.yomu.profile.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.yomu.profile.model.Profile;
import id.ac.ui.cs.advprog.yomu.profile.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_USERNAME = "prasetya";
    private static final String TEST_UNKNOWN = "unknown";

    @Mock
    private ProfileRepository profileRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ProfileServiceImpl profileService;

    private Profile sampleProfile;

    @BeforeEach
    void setUp() {
        sampleProfile = Profile.builder()
                .userId(TEST_USER_ID)
                .username(TEST_USERNAME)
                .displayName("Prasetya")
                .bio("Existing bio text")
                .joinedAt(LocalDateTime.of(2026, 5, 1, 10, 0))
                .completedTexts(5)
                .totalMinutes(40)
                .quizAccuracy(85)
                .showcaseAchievementsJson("[]")
                .build();
    }

    @Test
    void testGetProfileByUserId() {
        when(profileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(sampleProfile));

        ProfileResponse response = profileService.getProfileByUserIdOrUsername(TEST_USER_ID);

        assertAll("profile response details",
            () -> assertNotNull(response, "Profile response should not be null"),
            () -> assertEquals(TEST_USER_ID, response.getUserId(), "User ID should match"),
            () -> assertEquals(TEST_USERNAME, response.getUsername(), "Username should match"),
            () -> assertEquals("Prasetya", response.getDisplayName(), "Display name should match"),
            () -> assertEquals("Existing bio text", response.getBio(), "Bio should match"),
            () -> assertEquals("Mei 2026", response.getJoinedDate(), "Formatted joined date should match"),
            () -> assertEquals(5, response.getReadingStats().getCompletedTexts(), "Completed texts count should match"),
            () -> assertEquals(40, response.getReadingStats().getTotalMinutes(), "Total minutes should match"),
            () -> assertEquals(85, response.getReadingStats().getQuizAccuracy(), "Quiz accuracy should match")
        );
    }

    @Test
    void testGetProfileByUsername() {
        when(profileRepository.findById(TEST_USERNAME)).thenReturn(Optional.empty());
        when(profileRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(sampleProfile));

        ProfileResponse response = profileService.getProfileByUserIdOrUsername(TEST_USERNAME);

        assertAll("profile response by username details",
            () -> assertNotNull(response, "Profile response by username should not be null"),
            () -> assertEquals(TEST_USER_ID, response.getUserId(), "User ID should match"),
            () -> assertEquals(TEST_USERNAME, response.getUsername(), "Username should match")
        );
    }

    @Test
    void testGetProfileNotFound() {
        when(profileRepository.findById(TEST_UNKNOWN)).thenReturn(Optional.empty());
        when(profileRepository.findByUsername(TEST_UNKNOWN)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            profileService.getProfileByUserIdOrUsername(TEST_UNKNOWN);
        }, "Should throw IllegalArgumentException when profile is not found");
    }

    @Test
    void testUpdateBioSuccessful() {
        when(profileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(sampleProfile));
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        ProfileResponse response = profileService.updateBio(TEST_USER_ID, "New premium bio!");

        assertAll("profile response after bio update details",
            () -> assertNotNull(response, "Profile response should not be null"),
            () -> assertEquals("New premium bio!", response.getBio(), "Bio should be updated successfully")
        );
    }

    @Test
    void testUpdateBioNotFound() {
        when(profileRepository.findById(TEST_UNKNOWN)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            profileService.updateBio(TEST_UNKNOWN, "New bio!");
        }, "Should throw IllegalArgumentException when updating bio of non-existent profile");
    }

    @Test
    void testUpdateBioExceedsMaxLength() {
        String longBio = "A".repeat(101);

        assertThrows(IllegalArgumentException.class, () -> {
            profileService.updateBio(TEST_USER_ID, longBio);
        }, "Should throw IllegalArgumentException when bio exceeds 100 characters");
    }
}
