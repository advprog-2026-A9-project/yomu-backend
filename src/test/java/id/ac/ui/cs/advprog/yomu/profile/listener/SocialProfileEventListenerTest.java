package id.ac.ui.cs.advprog.yomu.profile.listener;

import id.ac.ui.cs.advprog.yomu.profile.model.Profile;
import id.ac.ui.cs.advprog.yomu.profile.repository.ProfileRepository;
import id.ac.ui.cs.advprog.yomu.profile.service.ProfileService;
import id.ac.ui.cs.advprog.yomu.social.event.ClanNameChangedEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserDeleteClanEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserJoinClanEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserLeaveClanEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class SocialProfileEventListenerTest {

    private static final String TEST_USER_ID = "prasetya";
    private static final String TEST_USERNAME = "prasetya";
    private static final String TEST_CLAN_ID = "clan-456";
    private static final String TEST_CLAN_NAME = "Great Clan";
    private static final String TEST_CLAN_TIER = "GOLD";

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private SocialProfileEventListener socialProfileEventListener;

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
    void testOnUserJoinClan() {
        when(profileService.getOrCreateProfile(TEST_USER_ID)).thenReturn(sampleProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        UserJoinClanEvent event = new UserJoinClanEvent(this, TEST_USER_ID, TEST_CLAN_ID, TEST_CLAN_NAME,
                TEST_CLAN_TIER);
        socialProfileEventListener.onUserJoinClan(event);

        assertAll("clan join properties",
                () -> assertEquals(TEST_CLAN_ID, sampleProfile.getClanId(), "Clan ID should be set correctly"),
                () -> assertEquals(TEST_CLAN_NAME, sampleProfile.getClanName(), "Clan name should be set correctly"),
                () -> assertEquals(TEST_CLAN_TIER, sampleProfile.getClanTier(), "Clan tier should be set correctly"));
    }

    @Test
    void testOnUserLeaveClan() {
        sampleProfile.setClanId(TEST_CLAN_ID);
        sampleProfile.setClanName(TEST_CLAN_NAME);
        sampleProfile.setClanTier(TEST_CLAN_TIER);

        when(profileService.getOrCreateProfile(TEST_USER_ID)).thenReturn(sampleProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        UserLeaveClanEvent event = new UserLeaveClanEvent(this, TEST_USER_ID, TEST_CLAN_ID);
        socialProfileEventListener.onUserLeaveClan(event);

        assertAll("clan leave properties",
                () -> assertNull(sampleProfile.getClanId(), "Clan ID should be cleared"),
                () -> assertNull(sampleProfile.getClanName(), "Clan name should be cleared"),
                () -> assertNull(sampleProfile.getClanTier(), "Clan tier should be cleared"));
    }

    @Test
    void testOnUserDeleteClan() {
        sampleProfile.setClanId(TEST_CLAN_ID);
        sampleProfile.setClanName(TEST_CLAN_NAME);
        sampleProfile.setClanTier(TEST_CLAN_TIER);

        List<Profile> profiles = List.of(sampleProfile);
        when(profileRepository.findByClanId(TEST_CLAN_ID)).thenReturn(profiles);
        when(profileRepository.saveAll(anyList())).thenReturn(profiles);

        UserDeleteClanEvent event = new UserDeleteClanEvent(this, TEST_CLAN_ID);
        socialProfileEventListener.onUserDeleteClan(event);

        assertAll("clan delete properties",
                () -> assertNull(sampleProfile.getClanId(), "Clan ID should be cleared on delete"),
                () -> assertNull(sampleProfile.getClanName(), "Clan name should be cleared on delete"),
                () -> assertNull(sampleProfile.getClanTier(), "Clan tier should be cleared on delete"));
    }

    @Test
    void testOnClanNameChanged() {
        sampleProfile.setClanId(TEST_CLAN_ID);
        sampleProfile.setClanName(TEST_CLAN_NAME);

        List<Profile> profiles = List.of(sampleProfile);
        when(profileRepository.findByClanId(TEST_CLAN_ID)).thenReturn(profiles);
        when(profileRepository.saveAll(anyList())).thenReturn(profiles);

        ClanNameChangedEvent event = new ClanNameChangedEvent(this, TEST_CLAN_ID, "New Clan Name");
        socialProfileEventListener.onClanNameChanged(event);

        assertEquals("New Clan Name", sampleProfile.getClanName(), "Clan name should be updated to new name");
    }
}
