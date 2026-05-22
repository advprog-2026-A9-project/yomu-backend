package id.ac.ui.cs.advprog.yomu.profile.listener;

import java.time.LocalDateTime;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import id.ac.ui.cs.advprog.yomu.profile.model.Profile;
import id.ac.ui.cs.advprog.yomu.profile.repository.ProfileRepository;
import id.ac.ui.cs.advprog.yomu.profile.service.ProfileService;
import id.ac.ui.cs.advprog.yomu.social.event.ClanNameChangedEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserDeleteClanEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserJoinClanEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserLeaveClanEvent;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"null", "PMD"})
class SocialProfileEventListenerTest {

    private static final String TEST_USER_ID = "prasetya";
    private static final String TEST_USERNAME = "prasetya";
    private static final String TEST_CLAN_ID = "clan-456";
    private static final String TEST_CLAN_NAME = "Great Clan";
    private static final String TEST_CLAN_TIER = "GOLD";

    // Assertion Messages
    private static final String MSG_CLAN_ID_SET = "Clan ID should be set correctly";
    private static final String MSG_CLAN_NAME_SET = "Clan name should be set correctly";
    private static final String MSG_CLAN_TIER_SET = "Clan tier should be set correctly";
    private static final String MSG_CLAN_ID_CLEARED = "Clan ID should be cleared";
    private static final String MSG_CLAN_NAME_CLEARED = "Clan name should be cleared";
    private static final String MSG_CLAN_TIER_CLEARED = "Clan tier should be cleared";
    private static final String MSG_CLAN_NAME_UPDATED = "Clan name should be updated to new name";

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

    private void setLogLevel(Level level) {
        Logger logger = (Logger) LoggerFactory.getLogger(SocialProfileEventListener.class);
        logger.setLevel(level);
    }

    @Test
    void testOnUserJoinClan_InfoEnabled() {
        setLogLevel(Level.INFO);
        when(profileService.getOrCreateProfile(TEST_USER_ID)).thenReturn(sampleProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        UserJoinClanEvent event = new UserJoinClanEvent(this, TEST_USER_ID, TEST_CLAN_ID, TEST_CLAN_NAME,
                TEST_CLAN_TIER);
        socialProfileEventListener.onUserJoinClan(event);

        assertAll("clan join properties enabled",
                () -> assertEquals(TEST_CLAN_ID, sampleProfile.getClanId(), MSG_CLAN_ID_SET),
                () -> assertEquals(TEST_CLAN_NAME, sampleProfile.getClanName(), MSG_CLAN_NAME_SET),
                () -> assertEquals(TEST_CLAN_TIER, sampleProfile.getClanTier(), MSG_CLAN_TIER_SET));
        verify(profileRepository).save(sampleProfile);
    }

    @Test
    void testOnUserJoinClan_InfoDisabled() {
        setLogLevel(Level.OFF);
        when(profileService.getOrCreateProfile(TEST_USER_ID)).thenReturn(sampleProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        UserJoinClanEvent event = new UserJoinClanEvent(this, TEST_USER_ID, TEST_CLAN_ID, TEST_CLAN_NAME,
                TEST_CLAN_TIER);
        socialProfileEventListener.onUserJoinClan(event);

        assertEquals(TEST_CLAN_ID, sampleProfile.getClanId(), MSG_CLAN_ID_SET);
    }

    @Test
    void testOnUserLeaveClan_InfoEnabled() {
        setLogLevel(Level.INFO);
        sampleProfile.setClanId(TEST_CLAN_ID);
        sampleProfile.setClanName(TEST_CLAN_NAME);
        sampleProfile.setClanTier(TEST_CLAN_TIER);

        when(profileService.getOrCreateProfile(TEST_USER_ID)).thenReturn(sampleProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        UserLeaveClanEvent event = new UserLeaveClanEvent(this, TEST_USER_ID, TEST_CLAN_ID);
        socialProfileEventListener.onUserLeaveClan(event);

        assertAll("clan leave properties enabled",
                () -> assertNull(sampleProfile.getClanId(), MSG_CLAN_ID_CLEARED),
                () -> assertNull(sampleProfile.getClanName(), MSG_CLAN_NAME_CLEARED),
                () -> assertNull(sampleProfile.getClanTier(), MSG_CLAN_TIER_CLEARED));
        verify(profileRepository).save(sampleProfile);
    }

    @Test
    void testOnUserLeaveClan_InfoDisabled() {
        setLogLevel(Level.OFF);
        sampleProfile.setClanId(TEST_CLAN_ID);

        when(profileService.getOrCreateProfile(TEST_USER_ID)).thenReturn(sampleProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        UserLeaveClanEvent event = new UserLeaveClanEvent(this, TEST_USER_ID, TEST_CLAN_ID);
        socialProfileEventListener.onUserLeaveClan(event);

        assertNull(sampleProfile.getClanId(), MSG_CLAN_ID_CLEARED);
    }

    @Test
    void testOnUserDeleteClan_InfoEnabled() {
        setLogLevel(Level.INFO);
        sampleProfile.setClanId(TEST_CLAN_ID);
        sampleProfile.setClanName(TEST_CLAN_NAME);
        sampleProfile.setClanTier(TEST_CLAN_TIER);

        List<Profile> profiles = List.of(sampleProfile);
        when(profileRepository.findByClanId(TEST_CLAN_ID)).thenReturn(profiles);
        when(profileRepository.saveAll(anyList())).thenReturn(profiles);

        UserDeleteClanEvent event = new UserDeleteClanEvent(this, TEST_CLAN_ID);
        socialProfileEventListener.onUserDeleteClan(event);

        assertAll("clan delete properties enabled",
                () -> assertNull(sampleProfile.getClanId(), MSG_CLAN_ID_CLEARED),
                () -> assertNull(sampleProfile.getClanName(), MSG_CLAN_NAME_CLEARED),
                () -> assertNull(sampleProfile.getClanTier(), MSG_CLAN_TIER_CLEARED));
        verify(profileRepository).saveAll(profiles);
    }

    @Test
    void testOnUserDeleteClan_InfoDisabled() {
        setLogLevel(Level.OFF);
        sampleProfile.setClanId(TEST_CLAN_ID);

        List<Profile> profiles = List.of(sampleProfile);
        when(profileRepository.findByClanId(TEST_CLAN_ID)).thenReturn(profiles);
        when(profileRepository.saveAll(anyList())).thenReturn(profiles);

        UserDeleteClanEvent event = new UserDeleteClanEvent(this, TEST_CLAN_ID);
        socialProfileEventListener.onUserDeleteClan(event);

        assertNull(sampleProfile.getClanId(), MSG_CLAN_ID_CLEARED);
    }

    @Test
    void testOnClanNameChanged_InfoEnabled() {
        setLogLevel(Level.INFO);
        sampleProfile.setClanId(TEST_CLAN_ID);
        sampleProfile.setClanName(TEST_CLAN_NAME);

        List<Profile> profiles = List.of(sampleProfile);
        when(profileRepository.findByClanId(TEST_CLAN_ID)).thenReturn(profiles);
        when(profileRepository.saveAll(anyList())).thenReturn(profiles);

        ClanNameChangedEvent event = new ClanNameChangedEvent(this, TEST_CLAN_ID, "New Clan Name");
        socialProfileEventListener.onClanNameChanged(event);

        assertEquals("New Clan Name", sampleProfile.getClanName(), MSG_CLAN_NAME_UPDATED);
        verify(profileRepository).saveAll(profiles);
    }

    @Test
    void testOnClanNameChanged_InfoDisabled() {
        setLogLevel(Level.OFF);
        sampleProfile.setClanId(TEST_CLAN_ID);

        List<Profile> profiles = List.of(sampleProfile);
        when(profileRepository.findByClanId(TEST_CLAN_ID)).thenReturn(profiles);
        when(profileRepository.saveAll(anyList())).thenReturn(profiles);

        ClanNameChangedEvent event = new ClanNameChangedEvent(this, TEST_CLAN_ID, "New Clan Name");
        socialProfileEventListener.onClanNameChanged(event);

        assertEquals("New Clan Name", sampleProfile.getClanName(), MSG_CLAN_NAME_UPDATED);
    }
}
