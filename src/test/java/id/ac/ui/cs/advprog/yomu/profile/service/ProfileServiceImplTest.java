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

<<<<<<< HEAD
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
=======
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
>>>>>>> origin/staging

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

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
        when(profileRepository.findById(TEST_USERNAME)).thenReturn(Optional.of(sampleProfile));

        ProfileResponse response = profileService.getProfileByUserIdOrUsername(TEST_USERNAME);

        assertAll("profile response details",
                () -> assertNotNull(response, "Profile response should not be null"),
                () -> assertEquals(TEST_USERNAME, response.getUsername(), "Username should match"),
                () -> assertEquals("Prasetya", response.getDisplayName(), "Display name should match"),
                () -> assertEquals("Existing bio text", response.getBio(), "Bio should match"),
                () -> assertEquals("Mei 2026", response.getJoinedDate(), "Formatted joined date should match"),
                () -> assertEquals(5, response.getReadingStats().getCompletedTexts(),
                        "Completed texts count should match"),
                () -> assertEquals(40, response.getReadingStats().getTotalMinutes(), "Total minutes should match"),
                () -> assertEquals(85, response.getReadingStats().getQuizAccuracy(), "Quiz accuracy should match"));
    }

    @Test
    void testGetProfileNotFound() {
        when(profileRepository.findById(TEST_UNKNOWN)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            profileService.getProfileByUserIdOrUsername(TEST_UNKNOWN);
        }, "Should throw IllegalArgumentException when profile is not found");
    }

    @Test
    void testUpdateBioSuccessful() {
        when(profileRepository.findById(TEST_USERNAME)).thenReturn(Optional.of(sampleProfile));
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        ProfileResponse response = profileService.updateBio(TEST_USERNAME, "New premium bio!");

        assertAll("profile response after bio update details",
                () -> assertNotNull(response, "Profile response should not be null"),
                () -> assertEquals("New premium bio!", response.getBio(), "Bio should be updated successfully"));
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
            profileService.updateBio(TEST_USERNAME, longBio);
        }, "Should throw IllegalArgumentException when bio exceeds 100 characters");
    }
<<<<<<< HEAD

    @Test
    void testGetOrCreateProfile_WhenAlreadyExists_ShouldReturnExisting() {
        when(profileRepository.findById(TEST_USERNAME)).thenReturn(Optional.of(sampleProfile));

        Profile result = profileService.getOrCreateProfile(TEST_USERNAME, "Custom Name");

        assertAll("Verify existing profile retrieval",
                () -> assertNotNull(result, "Resulting profile should not be null"),
                () -> assertEquals(TEST_USERNAME, result.getUsername(), "Username should match TEST_USERNAME"),
                () -> verify(profileRepository, never()).save(any()));
    }

    @Test
    void testGetOrCreateProfile_WhenDoesNotExist_ShouldCreateAndSave() {
        when(profileRepository.findById(TEST_USERNAME)).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Profile result = profileService.getOrCreateProfile(TEST_USERNAME, "Custom Name");

        assertAll("Verify new profile creation",
                () -> assertNotNull(result, "Resulting profile should not be null"),
                () -> assertEquals(TEST_USERNAME, result.getUsername(), "Username should match TEST_USERNAME"),
                () -> assertEquals("Custom Name", result.getDisplayName(), "Display name should match custom input"),
                () -> assertEquals("📖 Yomu avid reader | Seeking knowledge every single day.", result.getBio(), "Default bio should be assigned"),
                () -> verify(profileRepository).save(any(Profile.class)));
    }

    @Test
    void testGetOrCreateProfile_SingleArg_ShouldDelegate() {
        when(profileRepository.findById(TEST_USERNAME)).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Profile result = profileService.getOrCreateProfile(TEST_USERNAME);

        assertAll("Verify new profile delegation creation",
                () -> assertNotNull(result, "Resulting profile should not be null"),
                () -> assertEquals(TEST_USERNAME, result.getUsername(), "Username should match TEST_USERNAME"),
                () -> assertEquals("User " + TEST_USERNAME, result.getDisplayName(), "Display name should be generated from username"));
    }

    @Test
    void testFormatJoinedDate_WhenNull() {
        sampleProfile.setJoinedAt(null);
        when(profileRepository.findById(TEST_USERNAME)).thenReturn(Optional.of(sampleProfile));

        ProfileResponse response = profileService.getProfileByUserIdOrUsername(TEST_USERNAME);

        assertEquals("Mei 2026", response.getJoinedDate(), "Formatted joined date should default to May 2026 when null");
    }

    @Test
    void testFormatJoinedDate_WhenJanuari() {
        sampleProfile.setJoinedAt(LocalDateTime.of(2025, 1, 15, 12, 0));
        when(profileRepository.findById(TEST_USERNAME)).thenReturn(Optional.of(sampleProfile));

        ProfileResponse response = profileService.getProfileByUserIdOrUsername(TEST_USERNAME);

        assertEquals("Januari 2025", response.getJoinedDate(), "Formatted joined date should be Januari 2025");
    }

    @Test
    void testDeserializeShowcaseAchievements_WhenInvalidJson_ShouldCatchExceptionAndLog() {
        sampleProfile.setShowcaseAchievementsJson("invalid-json");
        when(profileRepository.findById(TEST_USERNAME)).thenReturn(Optional.of(sampleProfile));

        ProfileResponse response = profileService.getProfileByUserIdOrUsername(TEST_USERNAME);

        assertAll("Verify deserialization fallback on invalid json",
                () -> assertNotNull(response, "Response should not be null"),
                () -> assertTrue(response.getShowcaseAchievements().isEmpty(), "Showcase achievements list should be empty"));
    }

    @Test
    void testDeserializeShowcaseAchievements_WhenValidJson_ShouldReturnList() {
        sampleProfile.setShowcaseAchievementsJson("[{\"id\":\"ach-1\",\"name\":\"Gold Star\",\"description\":\"Read 10 articles\",\"tier\":\"GOLD\"}]");
        when(profileRepository.findById(TEST_USERNAME)).thenReturn(Optional.of(sampleProfile));

        ProfileResponse response = profileService.getProfileByUserIdOrUsername(TEST_USERNAME);

        assertAll("Verify deserialization of valid json",
                () -> assertNotNull(response, "Response should not be null"),
                () -> assertEquals(1, response.getShowcaseAchievements().size(), "Showcase achievements list size should match 1"),
                () -> assertEquals("ach-1", response.getShowcaseAchievements().get(0).getId(), "First achievement ID should match ach-1"));
    }
=======
>>>>>>> origin/staging
}
