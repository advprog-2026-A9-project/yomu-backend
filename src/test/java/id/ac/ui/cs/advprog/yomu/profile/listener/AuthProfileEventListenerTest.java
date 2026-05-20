package id.ac.ui.cs.advprog.yomu.profile.listener;

import id.ac.ui.cs.advprog.yomu.auth.event.UserCreatedEvent;
import id.ac.ui.cs.advprog.yomu.auth.event.UserUpdatedEvent;
import id.ac.ui.cs.advprog.yomu.profile.model.Profile;
import id.ac.ui.cs.advprog.yomu.profile.repository.ProfileRepository;
import id.ac.ui.cs.advprog.yomu.profile.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AuthProfileEventListenerTest {

    private static final String TEST_USER_ID = "prasetya";
    private static final String TEST_USERNAME = "prasetya";

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
                .displayName("Prasetya")
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
        when(profileService.getOrCreateProfile(TEST_USERNAME, "Prasetya")).thenReturn(sampleProfile);

        UserCreatedEvent event = new UserCreatedEvent(this, TEST_USER_ID, TEST_USERNAME, "Prasetya");
        authProfileEventListener.onUserCreated(event);

        verify(profileService).getOrCreateProfile(TEST_USERNAME, "Prasetya");
    }

    @Test
    void testOnUserUpdated() {
        when(profileService.getOrCreateProfile(TEST_USERNAME, "New Name")).thenReturn(sampleProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        UserUpdatedEvent event = new UserUpdatedEvent(this, TEST_USER_ID, TEST_USERNAME, "New Name");
        authProfileEventListener.onUserUpdated(event);

        verify(profileService).getOrCreateProfile(TEST_USERNAME, "New Name");
        verify(profileRepository).save(sampleProfile);
        assertEquals("New Name", sampleProfile.getDisplayName());
    }
}
