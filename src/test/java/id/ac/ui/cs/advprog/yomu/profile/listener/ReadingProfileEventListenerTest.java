package id.ac.ui.cs.advprog.yomu.profile.listener;

import id.ac.ui.cs.advprog.yomu.profile.model.Profile;
import id.ac.ui.cs.advprog.yomu.profile.repository.ProfileRepository;
import id.ac.ui.cs.advprog.yomu.profile.service.ProfileService;
import id.ac.ui.cs.advprog.yomu.reading.event.QuizCompletedEvent;
import id.ac.ui.cs.advprog.yomu.reading.event.ReadingCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ReadingProfileEventListenerTest {

    private static final String TEST_USER_ID = "prasetya";
    private static final String TEST_USERNAME = "prasetya";

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

    @Test
    void testOnQuizCompleted() {
        when(profileService.getOrCreateProfile(TEST_USER_ID)).thenReturn(sampleProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        QuizCompletedEvent event = new QuizCompletedEvent(TEST_USER_ID, 1L, 100, 4, 5);
        readingProfileEventListener.onQuizCompleted(event);

        assertAll("quiz completed stats",
                () -> assertEquals(0, sampleProfile.getCompletedTexts(), "Completed texts count should not increment"),
                () -> assertEquals(0, sampleProfile.getTotalMinutes(), "Total minutes should not increment"),
                () -> assertEquals(80, sampleProfile.getQuizAccuracy(), "Quiz accuracy should be 80%"),
                () -> assertEquals(4, sampleProfile.getCorrectAnswersSum(), "Correct answers sum should match"),
                () -> assertEquals(5, sampleProfile.getTotalQuestionsSum(), "Total questions sum should match"));
    }

    @Test
    void testOnReadingCompleted() {
        when(profileService.getOrCreateProfile(TEST_USER_ID)).thenReturn(sampleProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(sampleProfile);

        ReadingCompletedEvent event = new ReadingCompletedEvent(this, 1L, TEST_USER_ID);
        readingProfileEventListener.onReadingCompleted(event);

        assertAll("reading completed stats",
                () -> assertEquals(1, sampleProfile.getCompletedTexts(), "Completed texts count should increment by 1"),
                () -> assertEquals(8, sampleProfile.getTotalMinutes(), "Total minutes should increment by 8"));
    }
}
