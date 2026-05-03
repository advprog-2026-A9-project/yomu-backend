package id.ac.ui.cs.advprog.yomu.reading.service;

import id.ac.ui.cs.advprog.yomu.reading.dto.QuizAnswerRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizSubmissionRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizSubmissionResponse;
import id.ac.ui.cs.advprog.yomu.reading.model.Category;
import id.ac.ui.cs.advprog.yomu.reading.model.QuizOption;
import id.ac.ui.cs.advprog.yomu.reading.model.QuizQuestion;
import id.ac.ui.cs.advprog.yomu.reading.model.ReadingCompletion;
import id.ac.ui.cs.advprog.yomu.reading.model.ReadingText;
import id.ac.ui.cs.advprog.yomu.reading.repository.QuizQuestionRepository;
import id.ac.ui.cs.advprog.yomu.reading.repository.ReadingCompletionRepository;
import id.ac.ui.cs.advprog.yomu.reading.repository.ReadingTextRepository;
import id.ac.ui.cs.advprog.yomu.reading.event.QuizCompletedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
class QuizSubmissionServiceTest {

    private static final String USER_ID = "user-123";
    private static final String CATEGORY_NAME = "Edukasi";
    private static final String READING_TITLE = "Belajar Java";
    private static final String READING_CONTENT = "Isi bacaan";
    private static final String QUESTION_TEXT_1 = "Apa kepanjangan OOP?";
    private static final String QUESTION_TEXT_2 = "Java berjalan di mana?";

    @Mock
    private ReadingTextRepository readingTextRepository;

    @Mock
    private QuizQuestionRepository quizQuestionRepository;

    @Mock
    private ReadingCompletionRepository readingCompletionRepository;

    @InjectMocks
    private QuizSubmissionServiceImpl quizSubmissionService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void submitQuiz_WhenAnswersAreValid_ShouldReturnScoreAndSaveCompletion() {
        final Category category = new Category(1L, CATEGORY_NAME);
        final ReadingText readingText = new ReadingText(1L, READING_TITLE, READING_CONTENT, category);

        final QuizQuestion question1 = new QuizQuestion();
        question1.setId(10L);
        question1.setQuestionText(QUESTION_TEXT_1);
        question1.setReadingText(readingText);

        final QuizOption q1Correct = new QuizOption();
        q1Correct.setId(100L);
        q1Correct.setOptionText("Object Oriented Programming");
        q1Correct.setCorrect(true);
        q1Correct.setQuizQuestion(question1);

        final QuizOption q1Wrong = new QuizOption();
        q1Wrong.setId(101L);
        q1Wrong.setOptionText("Open Operational Protocol");
        q1Wrong.setCorrect(false);
        q1Wrong.setQuizQuestion(question1);

        question1.setOptions(List.of(q1Correct, q1Wrong));

        final QuizQuestion question2 = new QuizQuestion();
        question2.setId(20L);
        question2.setQuestionText(QUESTION_TEXT_2);
        question2.setReadingText(readingText);

        final QuizOption q2Correct = new QuizOption();
        q2Correct.setId(200L);
        q2Correct.setOptionText("JVM");
        q2Correct.setCorrect(true);
        q2Correct.setQuizQuestion(question2);

        final QuizOption q2Wrong = new QuizOption();
        q2Wrong.setId(201L);
        q2Wrong.setOptionText("Browser");
        q2Wrong.setCorrect(false);
        q2Wrong.setQuizQuestion(question2);

        question2.setOptions(List.of(q2Correct, q2Wrong));

        final QuizSubmissionRequest request = new QuizSubmissionRequest(
                List.of(
                        new QuizAnswerRequest(10L, 100L),
                        new QuizAnswerRequest(20L, 201L)
                )
        );

        when(readingTextRepository.findById(1L)).thenReturn(Optional.of(readingText));
        when(quizQuestionRepository.findByReadingTextId(1L)).thenReturn(List.of(question1, question2));
        when(readingCompletionRepository.existsByUserIdAndReadingTextId(USER_ID, 1L)).thenReturn(false);
        when(readingCompletionRepository.save(any(ReadingCompletion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final QuizSubmissionResponse response = quizSubmissionService.submitQuiz(1L, USER_ID, request);

        assertNotNull(response, "Response tidak boleh null");
        assertEquals(2, response.totalQuestions(), "Jumlah total soal harus 2");
        assertEquals(1, response.correctAnswers(), "Jumlah jawaban benar harus 1");
        assertEquals(50, response.score(), "Skor harus 50");
        assertEquals(true, response.completed(), "Completion harus true");

        verify(eventPublisher, times(1)).publishEvent(any(QuizCompletedEvent.class));
    }

    @Test
    void submitQuiz_WhenAlreadyCompleted_ShouldThrowException() {
        final QuizSubmissionRequest request = new QuizSubmissionRequest(List.of());

        when(readingCompletionRepository.existsByUserIdAndReadingTextId(USER_ID, 1L)).thenReturn(true);

        assertThrows(
                RuntimeException.class,
                () -> quizSubmissionService.submitQuiz(1L, USER_ID, request),
                "User yang sudah selesai tidak boleh submit lagi"
        );
    }

    @Test
    void hasCompletedQuiz_WhenCompletionExists_ShouldReturnTrue() {
        when(readingCompletionRepository.existsByUserIdAndReadingTextId(USER_ID, 1L)).thenReturn(true);

        final boolean completed = quizSubmissionService.hasCompletedQuiz(1L, USER_ID);

        assertEquals(true, completed, "Completion harus true");
    }
}