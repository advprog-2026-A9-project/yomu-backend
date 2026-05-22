package id.ac.ui.cs.advprog.yomu.profile.listener;

import java.time.LocalDateTime;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import id.ac.ui.cs.advprog.yomu.reading.event.QuizCompletedEvent;
import id.ac.ui.cs.advprog.yomu.reading.event.ReadingCompletedEvent;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"null", "PMD"})
class ReadingProfileEventListenerTest {

    private static final String TEST_USER_ID = "prasetya";
    private static final String TEST_USERNAME = "prasetya";

    // Assertion Messages
    private static final String MSG_COMPLETED_TEXTS = "Completed texts count should not increment";
    private static final String MSG_TOTAL_MINUTES = "Total minutes should not increment";
    private static final String MSG_QUIZ_ACCURACY = "Quiz accuracy should match expected";
    private static final String MSG_CORRECT_SUM = "Correct answers sum should match";
    private static final String MSG_TOTAL_SUM = "Total questions sum should match";
    private static final String MSG_SAVE_CALLED = "ProfileRepository.save should be called";

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private ReadingProfileEventListener readingProfileEventListener;

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
        Logger logger = (Logger) LoggerFactory.getLogger(ReadingProfileEventListener.class);
        logger.setLevel(level);
    }

    @Test
    void testOnQuizCompleted_InfoEnabled_QuestionsGreaterThanZero() {
        setLogLevel(Level.INFO);
        when(profileService.getOrCreateProfile(TEST_USER_ID)).thenReturn(sampleProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        QuizCompletedEvent event = new QuizCompletedEvent(TEST_USER_ID, 1L, 100, 4, 5);
        readingProfileEventListener.onQuizCompleted(event);

        assertAll("quiz completed stats",
                () -> assertEquals(0, sampleProfile.getCompletedTexts(), MSG_COMPLETED_TEXTS),
                () -> assertEquals(0, sampleProfile.getTotalMinutes(), MSG_TOTAL_MINUTES),
                () -> assertEquals(80, sampleProfile.getQuizAccuracy(), MSG_QUIZ_ACCURACY),
                () -> assertEquals(4, sampleProfile.getCorrectAnswersSum(), MSG_CORRECT_SUM),
                () -> assertEquals(5, sampleProfile.getTotalQuestionsSum(), MSG_TOTAL_SUM));
        verify(profileRepository).save(sampleProfile);
    }

    @Test
    void testOnQuizCompleted_InfoDisabled_QuestionsGreaterThanZero() {
        setLogLevel(Level.OFF);
        when(profileService.getOrCreateProfile(TEST_USER_ID)).thenReturn(sampleProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        QuizCompletedEvent event = new QuizCompletedEvent(TEST_USER_ID, 1L, 100, 4, 5);
        readingProfileEventListener.onQuizCompleted(event);

        assertEquals(80, sampleProfile.getQuizAccuracy(), MSG_QUIZ_ACCURACY);
    }

    @Test
    void testOnQuizCompleted_ZeroQuestions() {
        setLogLevel(Level.INFO);
        when(profileService.getOrCreateProfile(TEST_USER_ID)).thenReturn(sampleProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        QuizCompletedEvent event = new QuizCompletedEvent(TEST_USER_ID, 1L, 100, 0, 0);
        readingProfileEventListener.onQuizCompleted(event);

        assertAll("quiz completed stats with zero questions",
                () -> assertEquals(0, sampleProfile.getQuizAccuracy(), MSG_QUIZ_ACCURACY),
                () -> assertEquals(0, sampleProfile.getCorrectAnswersSum(), MSG_CORRECT_SUM),
                () -> assertEquals(0, sampleProfile.getTotalQuestionsSum(), MSG_TOTAL_SUM));
    }

    @Test
    void testOnReadingCompleted_InfoEnabled() {
        setLogLevel(Level.INFO);
        when(profileService.getOrCreateProfile(TEST_USER_ID)).thenReturn(sampleProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        ReadingCompletedEvent event = new ReadingCompletedEvent(this, 1L, TEST_USER_ID);
        readingProfileEventListener.onReadingCompleted(event);

        assertAll("reading completed stats",
                () -> assertEquals(1, sampleProfile.getCompletedTexts(), "Completed texts count should increment by 1"),
                () -> assertEquals(8, sampleProfile.getTotalMinutes(), "Total minutes should increment by 8"));
        verify(profileRepository).save(sampleProfile);
    }

    @Test
    void testOnReadingCompleted_InfoDisabled() {
        setLogLevel(Level.OFF);
        when(profileService.getOrCreateProfile(TEST_USER_ID)).thenReturn(sampleProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        ReadingCompletedEvent event = new ReadingCompletedEvent(this, 1L, TEST_USER_ID);
        readingProfileEventListener.onReadingCompleted(event);

        assertEquals(1, sampleProfile.getCompletedTexts(), "Completed texts count should increment by 1");
    }
}
