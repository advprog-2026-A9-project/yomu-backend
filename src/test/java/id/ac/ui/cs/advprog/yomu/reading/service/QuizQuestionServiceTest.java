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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
class QuizQuestionServiceTest {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_PELAJAR = "PELAJAR";
    private static final Long TEXT_ID = 1L;
    private static final Long QUESTION_ID = 10L;

    // PMD Fix: Menggunakan konstan untuk String yang berulang
    private static final String QUESTION_TEXT_OOP = "Apa kepanjangan OOP?";

    @Mock
    private QuizQuestionRepository quizQuestionRepository;

    @Mock
    private QuizOptionRepository quizOptionRepository;

    @Mock
    private ReadingTextRepository readingTextRepository;

    @InjectMocks
    private QuizQuestionServiceImpl quizQuestionService;

    // Dummy Objects
    private ReadingText readingText;
    private QuizQuestionRequest validRequest;
    private QuizQuestion savedQuestion;

    @BeforeEach
    void setUp() {
        Category category = new Category(1L, "Edukasi");
        readingText = new ReadingText(TEXT_ID, "Belajar Java", "Isi bacaan", category);

        // Setup Request dari Frontend
        QuizOptionRequest optionRequest1 = new QuizOptionRequest("Object Oriented Programming", true);
        QuizOptionRequest optionRequest2 = new QuizOptionRequest("Open Operational Protocol", false);
        validRequest = new QuizQuestionRequest(QUESTION_TEXT_OOP, List.of(optionRequest1, optionRequest2));

        // Setup Hasil Save ke Database
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

        savedQuestion.setOptions(List.of(savedOption1, savedOption2));
    }

    // ==========================================
    // TEST CREATE QUESTION
    // ==========================================

    @Test
    void createQuestion_WhenRoleIsAdminAndTextExists_ShouldSaveQuestion() {
        when(readingTextRepository.findById(TEXT_ID)).thenReturn(Optional.of(readingText));
        when(quizQuestionRepository.save(any(QuizQuestion.class))).thenReturn(savedQuestion);

        // TAMBAHAN: Kita harus memberitahu Mockito agar tidak me-return null saat opsi disimpan
        when(quizOptionRepository.save(any(QuizOption.class))).thenAnswer(invocation -> {
            QuizOption option = invocation.getArgument(0);
            option.setId(100L); // Berikan ID tiruan agar savedOpt.getId() tidak NullPointerException
            return option;
        });

        QuizQuestionResponse response = quizQuestionService.createQuestion(TEXT_ID, validRequest, ROLE_ADMIN);

        assertNotNull(response, "Response tidak boleh null");
        assertEquals(QUESTION_ID, response.id(), "ID question harus sesuai");
        assertEquals(QUESTION_TEXT_OOP, response.questionText(), "Question text harus sesuai");
        assertEquals(2, response.options().size(), "Jumlah option harus 2");

        verify(readingTextRepository, times(1)).findById(TEXT_ID);
        verify(quizQuestionRepository, times(1)).save(any(QuizQuestion.class));
        verify(quizOptionRepository, times(2)).save(any(QuizOption.class)); // Pastikan 2 opsi disave
    }

    @Test
    void createQuestion_WhenRoleIsAdminButReadingTextNotFound_ShouldThrowException() {
        when(readingTextRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> quizQuestionService.createQuestion(99L, validRequest, ROLE_ADMIN),
                "Harus melempar exception jika text bacaan induk tidak ditemukan"
        );

        verify(quizQuestionRepository, never()).save(any(QuizQuestion.class));
        verify(quizOptionRepository, never()).save(any(QuizOption.class));
    }

    @Test
    void createQuestion_WhenRoleIsPelajar_ShouldThrowException() {
        assertThrows(
                RuntimeException.class,
                () -> quizQuestionService.createQuestion(TEXT_ID, validRequest, ROLE_PELAJAR),
                "Pelajar tidak boleh membuat question"
        );

        verify(readingTextRepository, never()).findById(anyLong());
        verify(quizQuestionRepository, never()).save(any(QuizQuestion.class));
    }

    // ==========================================
    // TEST GET QUESTIONS
    // ==========================================

    @Test
    void getQuestionsByReadingId_ShouldReturnQuestionList() {
        when(quizQuestionRepository.findByReadingTextId(TEXT_ID)).thenReturn(List.of(savedQuestion));

        List<QuizQuestionResponse> responses = quizQuestionService.getQuestionsByReadingId(TEXT_ID);

        // PMD Fix: Menambahkan parameter String message pada asserts
        assertNotNull(responses, "Daftar respons soal tidak boleh null");
        assertEquals(1, responses.size(), "Ukuran daftar soal harus 1");
        assertEquals(QUESTION_TEXT_OOP, responses.get(0).questionText(), "Teks soal harus cocok dengan DB");
        assertEquals(2, responses.get(0).options().size(), "Jumlah opsi jawaban harus 2");
        verify(quizQuestionRepository, times(1)).findByReadingTextId(TEXT_ID);
    }

    // ==========================================
    // TEST DELETE QUESTION
    // ==========================================

    @Test
    void deleteQuestion_WhenRoleIsAdminAndQuestionExists_ShouldDeleteQuestion() {
        when(quizQuestionRepository.existsById(QUESTION_ID)).thenReturn(true);

        // PMD Fix: Menambahkan parameter String message pada assertDoesNotThrow
        assertDoesNotThrow(
                () -> quizQuestionService.deleteQuestion(QUESTION_ID, ROLE_ADMIN),
                "Operasi penghapusan oleh ADMIN tidak boleh melempar exception"
        );

        verify(quizQuestionRepository, times(1)).deleteById(QUESTION_ID);
    }

    @Test
    void deleteQuestion_WhenRoleIsAdminButQuestionDoesNotExist_ShouldThrowException() {
        when(quizQuestionRepository.existsById(99L)).thenReturn(false);

        assertThrows(
                RuntimeException.class,
                () -> quizQuestionService.deleteQuestion(99L, ROLE_ADMIN),
                "Harus melempar exception jika pertanyaan yang ingin dihapus tidak ada"
        );

        verify(quizQuestionRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteQuestion_WhenRoleIsPelajar_ShouldThrowException() {
        assertThrows(
                RuntimeException.class,
                () -> quizQuestionService.deleteQuestion(QUESTION_ID, ROLE_PELAJAR),
                "Pelajar tidak memiliki akses untuk menghapus soal"
        );

        verify(quizQuestionRepository, never()).deleteById(anyLong());
    }
}