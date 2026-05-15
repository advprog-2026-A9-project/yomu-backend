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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
class QuizSubmissionServiceTest {

    private static final String USER_ID = "user-123";
    private static final Long TEXT_ID = 1L;

    @Mock
    private ReadingTextRepository readingTextRepository;

    @Mock
    private QuizQuestionRepository quizQuestionRepository;

    @Mock
    private ReadingCompletionRepository readingCompletionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private QuizSubmissionServiceImpl quizSubmissionService;

    // Objek Mock Global
    private ReadingText readingText;
    private QuizQuestion question1;
    private QuizQuestion question2;
    private QuizSubmissionRequest validRequest;

    @BeforeEach
    void setUp() {
        Category category = new Category(1L, "Edukasi");
        readingText = new ReadingText(TEXT_ID, "Belajar Java", "Isi bacaan", category);

        // Setup Pertanyaan 1
        question1 = new QuizQuestion();
        question1.setId(10L);
        question1.setQuestionText("Apa kepanjangan OOP?");
        question1.setReadingText(readingText);

        QuizOption q1Correct = new QuizOption();
        q1Correct.setId(100L);
        q1Correct.setCorrect(true);

        QuizOption q1Wrong = new QuizOption();
        q1Wrong.setId(101L);
        q1Wrong.setCorrect(false);

        question1.setOptions(List.of(q1Correct, q1Wrong));

        // Setup Pertanyaan 2
        question2 = new QuizQuestion();
        question2.setId(20L);
        question2.setQuestionText("Java berjalan di mana?");
        question2.setReadingText(readingText);

        QuizOption q2Correct = new QuizOption();
        q2Correct.setId(200L);
        q2Correct.setCorrect(true);

        QuizOption q2Wrong = new QuizOption();
        q2Wrong.setId(201L);
        q2Wrong.setCorrect(false);

        question2.setOptions(List.of(q2Correct, q2Wrong));

        // Request Simulasi User: Menjawab Q1 Benar (100L), Q2 Salah (201L)
        validRequest = new QuizSubmissionRequest(
                List.of(
                        new QuizAnswerRequest(10L, 100L),
                        new QuizAnswerRequest(20L, 201L)
                )
        );
    }

    // ==========================================
    // TEST SUBMIT QUIZ
    // ==========================================

    @Test
    void submitQuiz_WhenAnswersAreValid_ShouldReturnScoreAndSaveCompletion() {
        when(readingTextRepository.findById(TEXT_ID)).thenReturn(Optional.of(readingText));
        when(quizQuestionRepository.findByReadingTextId(TEXT_ID)).thenReturn(List.of(question1, question2));
        when(readingCompletionRepository.existsByUserIdAndReadingTextId(USER_ID, TEXT_ID)).thenReturn(false);

        // Return object yang sama persis saat di-save
        when(readingCompletionRepository.save(any(ReadingCompletion.class))).thenAnswer(i -> i.getArgument(0));

        QuizSubmissionResponse response = quizSubmissionService.submitQuiz(TEXT_ID, USER_ID, validRequest);

        assertNotNull(response, "Response tidak boleh null");
        assertEquals(2, response.totalQuestions(), "Jumlah total soal harus 2");
        assertEquals(1, response.correctAnswers(), "Jumlah jawaban benar harus 1");
        assertEquals(50, response.score(), "Skor harus 50 (1 benar dari 2 soal)");
        assertTrue(response.completed(), "Completion harus true");

        // Pastikan tabel relasi ke-5 tersimpan
        verify(readingCompletionRepository, times(1)).save(any(ReadingCompletion.class));
        // Pastikan event broadcasting ke modul lain berjalan
        verify(eventPublisher, times(1)).publishEvent(any(QuizCompletedEvent.class));
    }

    @Test
    void submitQuiz_WhenAlreadyCompleted_ShouldThrowException() {
        // Skema cegah kecurangan: User sudah pernah submit
        when(readingCompletionRepository.existsByUserIdAndReadingTextId(USER_ID, TEXT_ID)).thenReturn(true);

        assertThrows(
                RuntimeException.class,
                () -> quizSubmissionService.submitQuiz(TEXT_ID, USER_ID, validRequest),
                "User yang sudah selesai tidak boleh submit lagi"
        );

        verify(readingCompletionRepository, never()).save(any(ReadingCompletion.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void submitQuiz_WhenReadingTextNotFound_ShouldThrowException() {
        // Skema Error: Frontend ngirim ID bacaan ngawur
        when(readingCompletionRepository.existsByUserIdAndReadingTextId(USER_ID, 99L)).thenReturn(false);
        when(readingTextRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> quizSubmissionService.submitQuiz(99L, USER_ID, validRequest),
                "Harus exception jika reading text tidak ada"
        );

        verify(readingCompletionRepository, never()).save(any(ReadingCompletion.class));
    }

    // ==========================================
    // TEST HAS COMPLETED QUIZ
    // ==========================================

    @Test
    void hasCompletedQuiz_WhenCompletionExists_ShouldReturnTrue() {
        when(readingCompletionRepository.existsByUserIdAndReadingTextId(USER_ID, TEXT_ID)).thenReturn(true);
        boolean completed = quizSubmissionService.hasCompletedQuiz(TEXT_ID, USER_ID);
        assertTrue(completed, "Harus mengembalikan true jika sudah terekam di tabel completions");
    }

    @Test
    void hasCompletedQuiz_WhenCompletionDoesNotExist_ShouldReturnFalse() {
        when(readingCompletionRepository.existsByUserIdAndReadingTextId(USER_ID, TEXT_ID)).thenReturn(false);
        boolean completed = quizSubmissionService.hasCompletedQuiz(TEXT_ID, USER_ID);
        assertFalse(completed, "Harus mengembalikan false jika belum pernah dikerjakan");
    }
}