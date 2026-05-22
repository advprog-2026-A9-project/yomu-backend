package id.ac.ui.cs.advprog.yomu.profile.listener;

import java.time.LocalDateTime;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import id.ac.ui.cs.advprog.yomu.gamification.event.UserShowcaseAchievementChangedEvent;
import id.ac.ui.cs.advprog.yomu.profile.model.Profile;
import id.ac.ui.cs.advprog.yomu.profile.repository.ProfileRepository;
import id.ac.ui.cs.advprog.yomu.profile.service.ProfileService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"null", "PMD"})
class GamificationProfileEventListenerTest {

    private static final String TEST_USER_ID = "prasetya";
    private static final String TEST_USERNAME = "prasetya";

    // Assertion Messages
    private static final String MSG_CONTAINS_ACH = "Showcase JSON should contain the achievement name";
    private static final String MSG_EMPTY_JSON = "Showcase JSON should be empty array on null/empty achievements";
    private static final String MSG_SAVE_CALLED = "ProfileRepository.save should be called";

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileService profileService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private GamificationProfileEventListener gamificationProfileEventListener;

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

    private void setLogLevel(Level level) {
        Logger logger = (Logger) LoggerFactory.getLogger(GamificationProfileEventListener.class);
        logger.setLevel(level);
    }

    @Test
    void testOnUserShowcaseAchievementChanged_InfoEnabled() {
        setLogLevel(Level.INFO);
        when(profileService.getOrCreateProfile(TEST_USER_ID)).thenReturn(sampleProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        UserShowcaseAchievementChangedEvent.ShowcaseAchievementInfo info =
                new UserShowcaseAchievementChangedEvent.ShowcaseAchievementInfo(
                        "ach-1", "First Quiz", "Complete a quiz", "GOLD");

        UserShowcaseAchievementChangedEvent event =
                new UserShowcaseAchievementChangedEvent(TEST_USER_ID, List.of(info));
        gamificationProfileEventListener.onUserShowcaseAchievementChanged(event);

        assertNotNull(sampleProfile.getShowcaseAchievementsJson(), "JSON should not be null");
        assertTrue(sampleProfile.getShowcaseAchievementsJson().contains("First Quiz"), MSG_CONTAINS_ACH);
        verify(profileRepository).save(sampleProfile);
    }

    @Test
    void testOnUserShowcaseAchievementChanged_InfoDisabled() {
        setLogLevel(Level.OFF);
        when(profileService.getOrCreateProfile(TEST_USER_ID)).thenReturn(sampleProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        UserShowcaseAchievementChangedEvent.ShowcaseAchievementInfo info =
                new UserShowcaseAchievementChangedEvent.ShowcaseAchievementInfo(
                        "ach-1", "First Quiz", "Complete a quiz", "GOLD");

        UserShowcaseAchievementChangedEvent event =
                new UserShowcaseAchievementChangedEvent(TEST_USER_ID, List.of(info));
        gamificationProfileEventListener.onUserShowcaseAchievementChanged(event);

        assertTrue(sampleProfile.getShowcaseAchievementsJson().contains("First Quiz"), MSG_CONTAINS_ACH);
    }

    @Test
    void testOnUserShowcaseAchievementChanged_NullAchievements() {
        setLogLevel(Level.INFO);
        when(profileService.getOrCreateProfile(TEST_USER_ID)).thenReturn(sampleProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        UserShowcaseAchievementChangedEvent event =
                new UserShowcaseAchievementChangedEvent(TEST_USER_ID, null);
        gamificationProfileEventListener.onUserShowcaseAchievementChanged(event);

        assertEquals("[]", sampleProfile.getShowcaseAchievementsJson(), MSG_EMPTY_JSON);
    }

    @Test
    void testOnUserShowcaseAchievementChanged_SerializationException_ErrorEnabled() throws JsonProcessingException {
        setLogLevel(Level.ERROR);
        when(profileService.getOrCreateProfile(TEST_USER_ID)).thenReturn(sampleProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        doThrow(new RuntimeException("Fake serialization error")).when(objectMapper).writeValueAsString(any());

        UserShowcaseAchievementChangedEvent.ShowcaseAchievementInfo info =
                new UserShowcaseAchievementChangedEvent.ShowcaseAchievementInfo(
                        "ach-1", "First Quiz", "Complete a quiz", "GOLD");

        UserShowcaseAchievementChangedEvent event =
                new UserShowcaseAchievementChangedEvent(TEST_USER_ID, List.of(info));
        gamificationProfileEventListener.onUserShowcaseAchievementChanged(event);

        assertEquals("[]", sampleProfile.getShowcaseAchievementsJson(), MSG_EMPTY_JSON);
    }

    @Test
    void testOnUserShowcaseAchievementChanged_SerializationException_ErrorDisabled() throws JsonProcessingException {
        setLogLevel(Level.OFF);
        when(profileService.getOrCreateProfile(TEST_USER_ID)).thenReturn(sampleProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        doThrow(new RuntimeException("Fake serialization error")).when(objectMapper).writeValueAsString(any());

        UserShowcaseAchievementChangedEvent.ShowcaseAchievementInfo info =
                new UserShowcaseAchievementChangedEvent.ShowcaseAchievementInfo(
                        "ach-1", "First Quiz", "Complete a quiz", "GOLD");

        UserShowcaseAchievementChangedEvent event =
                new UserShowcaseAchievementChangedEvent(TEST_USER_ID, List.of(info));
        gamificationProfileEventListener.onUserShowcaseAchievementChanged(event);

        assertEquals("[]", sampleProfile.getShowcaseAchievementsJson(), MSG_EMPTY_JSON);
    }
}
