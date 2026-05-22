package id.ac.ui.cs.advprog.yomu.reading.service;

import id.ac.ui.cs.advprog.yomu.reading.dto.QuizOptionRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizQuestionRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizQuestionResponse;
import id.ac.ui.cs.advprog.yomu.reading.model.Category;
import id.ac.ui.cs.advprog.yomu.reading.model.QuizOption;
import id.ac.ui.cs.advprog.yomu.reading.model.QuizQuestion;
import id.ac.ui.cs.advprog.yomu.reading.model.ReadingText;
import id.ac.ui.cs.advprog.yomu.reading.repository.QuizOptionRepository;
import id.ac.ui.cs.advprog.yomu.reading.repository.QuizQuestionRepository;
import id.ac.ui.cs.advprog.yomu.reading.repository.ReadingTextRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
class QuizQuestionServiceTest {

    private static final Long TEXT_ID = 1L;
    private static final Long QUESTION_ID = 10L;
    private static final String QUESTION_TEXT_OOP = "Apa kepanjangan OOP?";

    @Mock
    private QuizQuestionRepository quizQuestionRepository;

    @Mock
    private QuizOptionRepository quizOptionRepository;

    @Mock
    private ReadingTextRepository readingTextRepository;

    @InjectMocks
    private QuizQuestionServiceImpl quizQuestionService;

    private ReadingText readingText;
    private QuizQuestionRequest validRequest;
    private QuizQuestion savedQuestion;

    @BeforeEach
    void setUp() {
        Category category = new Category(1L, "Edukasi");
        readingText = new ReadingText(TEXT_ID, "Belajar Java", "Isi bacaan", category);

        QuizOptionRequest optionRequest1 = new QuizOptionRequest("Object Oriented Programming", true);
        QuizOptionRequest optionRequest2 = new QuizOptionRequest("Open Operational Protocol", false);
        validRequest = new QuizQuestionRequest(QUESTION_TEXT_OOP, List.of(optionRequest1, optionRequest2));

        savedQuestion = new QuizQuestion();
        savedQuestion.setId(QUESTION_ID);
        savedQuestion.setQuestionText(QUESTION_TEXT_OOP);
        savedQuestion.setReadingText(readingText);

        QuizOption savedOption1 = new QuizOption();
        savedOption1.setId(100L);
        savedOption1.setOptionText("Object Oriented Programming");
        savedOption1.setCorrect(true);
        savedOption1.setQuizQuestion(savedQuestion);

        QuizOption savedOption2 = new QuizOption();
        savedOption2.setId(101L);
        savedOption2.setOptionText("Open Operational Protocol");
        savedOption2.setCorrect(false);
        savedOption2.setQuizQuestion(savedQuestion);

        // PMD Fix: Gunakan mutable list agar .clear() saat update tidak melempar UnsupportedOperationException
        savedQuestion.setOptions(new ArrayList<>(List.of(savedOption1, savedOption2)));
    }

    @Test
    void createQuestion_WhenTextExists_ShouldSaveQuestion() {
        when(readingTextRepository.findById(TEXT_ID)).thenReturn(Optional.of(readingText));
        when(quizQuestionRepository.save(any(QuizQuestion.class))).thenReturn(savedQuestion);

        when(quizOptionRepository.save(any(QuizOption.class))).thenAnswer(invocation -> {
            QuizOption option = invocation.getArgument(0);
            option.setId(100L);
            return option;
        });

        QuizQuestionResponse response = quizQuestionService.createQuestion(TEXT_ID, validRequest);

        assertNotNull(response, "Response tidak boleh null");
        assertEquals(QUESTION_ID, response.id(), "ID question harus sesuai");
        verify(readingTextRepository, times(1)).findById(TEXT_ID);
        verify(quizQuestionRepository, times(1)).save(any(QuizQuestion.class));
        verify(quizOptionRepository, times(2)).save(any(QuizOption.class));
    }

    @Test
    void createQuestion_WhenReadingTextNotFound_ShouldThrowException() {
        when(readingTextRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> quizQuestionService.createQuestion(99L, validRequest),
                "Harus melempar exception jika text bacaan induk tidak ditemukan"
        );

        verify(quizQuestionRepository, never()).save(any(QuizQuestion.class));
    }

    @Test
    void getQuestionsByReadingId_ShouldReturnQuestionList() {
        when(quizQuestionRepository.findByReadingTextId(TEXT_ID)).thenReturn(List.of(savedQuestion));

        List<QuizQuestionResponse> responses = quizQuestionService.getQuestionsByReadingId(TEXT_ID);

        assertNotNull(responses, "Daftar respons soal tidak boleh null");
        assertEquals(1, responses.size(), "Ukuran daftar soal harus 1");
        verify(quizQuestionRepository, times(1)).findByReadingTextId(TEXT_ID);
    }

    @Test
    void updateQuestion_WhenQuestionExists_ShouldUpdateAndReturnResponse() {
        when(quizQuestionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(savedQuestion));
        when(quizQuestionRepository.save(any(QuizQuestion.class))).thenReturn(savedQuestion);

        when(quizOptionRepository.save(any(QuizOption.class))).thenAnswer(invocation -> {
            QuizOption option = invocation.getArgument(0);
            option.setId(200L);
            return option;
        });

        QuizQuestionResponse response = quizQuestionService.updateQuestion(QUESTION_ID, validRequest);

        assertNotNull(response, "Respons tidak boleh null setelah di-update");
        assertEquals(QUESTION_ID, response.id(), "ID question harus sesuai");
        verify(quizQuestionRepository, times(1)).findById(QUESTION_ID);
        verify(quizOptionRepository, times(1)).deleteAll(anyList()); // Verifikasi opsi lama dihapus
        verify(quizQuestionRepository, times(1)).save(any(QuizQuestion.class));
        verify(quizOptionRepository, times(2)).save(any(QuizOption.class)); // Verifikasi opsi baru disave
    }

    @Test
    void updateQuestion_WhenQuestionDoesNotExist_ShouldThrowException() {
        when(quizQuestionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> quizQuestionService.updateQuestion(99L, validRequest),
                "Harus melempar exception jika pertanyaan yang ingin diupdate tidak ditemukan"
        );

        verify(quizOptionRepository, never()).deleteAll(anyList());
        verify(quizQuestionRepository, never()).save(any(QuizQuestion.class));
    }

    @Test
    void deleteQuestion_WhenQuestionExists_ShouldDeleteQuestion() {
        when(quizQuestionRepository.existsById(QUESTION_ID)).thenReturn(true);

        assertDoesNotThrow(
                () -> quizQuestionService.deleteQuestion(QUESTION_ID),
                "Operasi penghapusan tidak boleh melempar exception"
        );

        verify(quizQuestionRepository, times(1)).deleteById(QUESTION_ID);
    }

    @Test
    void deleteQuestion_WhenQuestionDoesNotExist_ShouldThrowException() {
        when(quizQuestionRepository.existsById(99L)).thenReturn(false);

        assertThrows(
                RuntimeException.class,
                () -> quizQuestionService.deleteQuestion(99L),
                "Harus melempar exception jika pertanyaan yang ingin dihapus tidak ada"
        );

        verify(quizQuestionRepository, never()).deleteById(anyLong());
    }
}