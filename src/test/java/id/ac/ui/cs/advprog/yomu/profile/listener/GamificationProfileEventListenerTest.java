package id.ac.ui.cs.advprog.yomu.profile.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.gamification.event.UserShowcaseAchievementChangedEvent;
import id.ac.ui.cs.advprog.yomu.profile.model.Profile;
import id.ac.ui.cs.advprog.yomu.profile.repository.ProfileRepository;
import id.ac.ui.cs.advprog.yomu.profile.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class GamificationProfileEventListenerTest {

    private static final String TEST_USER_ID = "prasetya";
    private static final String TEST_USERNAME = "prasetya";

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

    @Test
    void testOnUserShowcaseAchievementChanged() {
        when(profileService.getOrCreateProfile(TEST_USER_ID)).thenReturn(sampleProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        UserShowcaseAchievementChangedEvent.ShowcaseAchievementInfo info =
                new UserShowcaseAchievementChangedEvent.ShowcaseAchievementInfo(
                        "ach-1", "First Quiz", "Complete a quiz", "GOLD");

        UserShowcaseAchievementChangedEvent event =
                new UserShowcaseAchievementChangedEvent(TEST_USER_ID, List.of(info));
        gamificationProfileEventListener.onUserShowcaseAchievementChanged(event);

        assertTrue(sampleProfile.getShowcaseAchievementsJson().contains("First Quiz"),
                "Showcase JSON should contain the achievement name");
    }
}
