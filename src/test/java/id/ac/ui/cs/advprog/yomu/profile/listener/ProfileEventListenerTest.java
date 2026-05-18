package id.ac.ui.cs.advprog.yomu.profile.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.auth.event.UserCreatedEvent;
import id.ac.ui.cs.advprog.yomu.auth.event.UserUpdatedEvent;
import id.ac.ui.cs.advprog.yomu.gamification.event.UserShowcaseAchievementChangedEvent;
import id.ac.ui.cs.advprog.yomu.profile.model.Profile;
import id.ac.ui.cs.advprog.yomu.profile.repository.ProfileRepository;
import id.ac.ui.cs.advprog.yomu.reading.event.QuizCompletedEvent;
import id.ac.ui.cs.advprog.yomu.social.event.ClanNameChangedEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserDeleteClanEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserJoinClanEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserLeaveClanEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileEventListenerTest {

    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_USERNAME = "prasetya";
    private static final String TEST_CLAN_ID = "clan-456";
    private static final String TEST_CLAN_NAME = "Great Clan";
    private static final String TEST_CLAN_TIER = "GOLD";

    @Mock
    private ProfileRepository profileRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ProfileEventListener profileEventListener;

    private Profile sampleProfile;

    @BeforeEach
    void setUp() {
        sampleProfile = Profile.builder()
                .userId(TEST_USER_ID)
                .username(TEST_USERNAME)
                .displayName("Prasetya")
                .bio("Existing bio text")
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
        when(profileRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        UserCreatedEvent event = new UserCreatedEvent(this, TEST_USER_ID, TEST_USERNAME, "Prasetya");
        profileEventListener.onUserCreated(event);

        verify(profileRepository, times(1)).save(any(Profile.class));
    }

    @Test
    void testOnUserUpdated() {
        when(profileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(sampleProfile));
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        UserUpdatedEvent event = new UserUpdatedEvent(this, TEST_USER_ID, "newusername", "New Name");
        profileEventListener.onUserUpdated(event);

        assertAll("profile update properties",
            () -> assertEquals("newusername", sampleProfile.getUsername(), "Username should match updated username"),
            () -> assertEquals("New Name", sampleProfile.getDisplayName(), "Display name should match updated display name")
        );
    }

    @Test
    void testOnQuizCompleted() {
        when(profileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(sampleProfile));
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        QuizCompletedEvent event = new QuizCompletedEvent(TEST_USER_ID, 1L, 100, 4, 5);
        profileEventListener.onQuizCompleted(event);

        assertAll("quiz completed stats",
            () -> assertEquals(1, sampleProfile.getCompletedTexts(), "Completed texts count should increment by 1"),
            () -> assertEquals(8, sampleProfile.getTotalMinutes(), "Total minutes should increment by 8"),
            () -> assertEquals(80, sampleProfile.getQuizAccuracy(), "Quiz accuracy should be 80%"),
            () -> assertEquals(4, sampleProfile.getCorrectAnswersSum(), "Correct answers sum should match"),
            () -> assertEquals(5, sampleProfile.getTotalQuestionsSum(), "Total questions sum should match")
        );
    }

    @Test
    void testOnUserJoinClan() {
        when(profileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(sampleProfile));
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        UserJoinClanEvent event = new UserJoinClanEvent(this, TEST_USER_ID, TEST_CLAN_ID, TEST_CLAN_NAME, TEST_CLAN_TIER);
        profileEventListener.onUserJoinClan(event);

        assertAll("clan join properties",
            () -> assertEquals(TEST_CLAN_ID, sampleProfile.getClanId(), "Clan ID should be set correctly"),
            () -> assertEquals(TEST_CLAN_NAME, sampleProfile.getClanName(), "Clan name should be set correctly"),
            () -> assertEquals(TEST_CLAN_TIER, sampleProfile.getClanTier(), "Clan tier should be set correctly")
        );
    }

    @Test
    void testOnUserLeaveClan() {
        sampleProfile.setClanId(TEST_CLAN_ID);
        sampleProfile.setClanName(TEST_CLAN_NAME);
        sampleProfile.setClanTier(TEST_CLAN_TIER);

        when(profileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(sampleProfile));
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        UserLeaveClanEvent event = new UserLeaveClanEvent(this, TEST_USER_ID, TEST_CLAN_ID);
        profileEventListener.onUserLeaveClan(event);

        assertAll("clan leave properties",
            () -> assertNull(sampleProfile.getClanId(), "Clan ID should be cleared"),
            () -> assertNull(sampleProfile.getClanName(), "Clan name should be cleared"),
            () -> assertNull(sampleProfile.getClanTier(), "Clan tier should be cleared")
        );
    }

    @Test
    void testOnUserDeleteClan() {
        sampleProfile.setClanId(TEST_CLAN_ID);
        sampleProfile.setClanName(TEST_CLAN_NAME);
        sampleProfile.setClanTier(TEST_CLAN_TIER);

        List<Profile> profiles = List.of(sampleProfile);
        when(profileRepository.findByClanId(TEST_CLAN_ID)).thenReturn(profiles);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        UserDeleteClanEvent event = new UserDeleteClanEvent(this, TEST_CLAN_ID);
        profileEventListener.onUserDeleteClan(event);

        assertAll("clan delete properties",
            () -> assertNull(sampleProfile.getClanId(), "Clan ID should be cleared on delete"),
            () -> assertNull(sampleProfile.getClanName(), "Clan name should be cleared on delete"),
            () -> assertNull(sampleProfile.getClanTier(), "Clan tier should be cleared on delete")
        );
    }

    @Test
    void testOnClanNameChanged() {
        sampleProfile.setClanId(TEST_CLAN_ID);
        sampleProfile.setClanName(TEST_CLAN_NAME);

        List<Profile> profiles = List.of(sampleProfile);
        when(profileRepository.findByClanId(TEST_CLAN_ID)).thenReturn(profiles);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        ClanNameChangedEvent event = new ClanNameChangedEvent(this, TEST_CLAN_ID, "New Clan Name");
        profileEventListener.onClanNameChanged(event);

        assertEquals("New Clan Name", sampleProfile.getClanName(), "Clan name should be updated to new name");
    }

    @Test
    void testOnUserShowcaseAchievementChanged() {
        when(profileRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(sampleProfile));
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        List<UserShowcaseAchievementChangedEvent.ShowcaseAchievementInfo> achievements = new ArrayList<>();
        achievements.add(new UserShowcaseAchievementChangedEvent.ShowcaseAchievementInfo(
            "ach-1", "First Quiz", "Complete a quiz", "GOLD", "text-amber-400"
        ));

        UserShowcaseAchievementChangedEvent event = new UserShowcaseAchievementChangedEvent(TEST_USER_ID, achievements);
        profileEventListener.onUserShowcaseAchievementChanged(event);

        assertTrue(sampleProfile.getShowcaseAchievementsJson().contains("First Quiz"), "Showcase JSON should contain the achievement name");
    }
}
