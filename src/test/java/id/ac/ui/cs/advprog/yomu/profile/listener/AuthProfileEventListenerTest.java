package id.ac.ui.cs.advprog.yomu.profile.listener;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import id.ac.ui.cs.advprog.yomu.auth.event.UserCreatedEvent;
import id.ac.ui.cs.advprog.yomu.auth.event.UserUpdatedEvent;
import id.ac.ui.cs.advprog.yomu.profile.model.Profile;
import id.ac.ui.cs.advprog.yomu.profile.repository.ProfileRepository;
import id.ac.ui.cs.advprog.yomu.profile.service.ProfileService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AuthProfileEventListenerTest {

    private static final String TEST_USER_ID = "prasetya";
    private static final String TEST_USERNAME = "prasetya";
    private static final String TEST_DISPLAY_NAME = "Prasetya";
    private static final String UPDATED_DISPLAY_NAME = "New Name";

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private AuthProfileEventListener authProfileEventListener;

    private Profile sampleProfile;

    @BeforeEach
    void setUp() {
        sampleProfile = Profile.builder()
                .username(TEST_USERNAME)
                .displayName(TEST_DISPLAY_NAME)
                .joinedAt(LocalDateTime.now())
                .completedTexts(0)
                .totalMinutes(0)
                .quizAccuracy(0)
                .correctAnswersSum(0)
                .totalQuestionsSum(0)
                .showcaseAchievementsJson("[]")
                .build();
    }

    @Test
    void testOnUserCreated() {
        when(profileService.getOrCreateProfile(TEST_USERNAME, TEST_DISPLAY_NAME)).thenReturn(sampleProfile);

        UserCreatedEvent event = new UserCreatedEvent(this, TEST_USER_ID, TEST_USERNAME, TEST_DISPLAY_NAME);
        authProfileEventListener.onUserCreated(event);

        assertEquals(TEST_DISPLAY_NAME, sampleProfile.getDisplayName(), "display name should remain the same");
    }

    @Test
    void testOnUserUpdated() {
        when(profileService.getOrCreateProfile(TEST_USERNAME, UPDATED_DISPLAY_NAME)).thenReturn(sampleProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        UserUpdatedEvent event = new UserUpdatedEvent(this, TEST_USER_ID, TEST_USERNAME, UPDATED_DISPLAY_NAME);
        authProfileEventListener.onUserUpdated(event);

        assertEquals(UPDATED_DISPLAY_NAME, sampleProfile.getDisplayName(), "display name should be updated");
    }
}
