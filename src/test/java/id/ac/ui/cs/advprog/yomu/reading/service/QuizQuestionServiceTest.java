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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
class QuizQuestionServiceTest {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_PELAJAR = "PELAJAR";
    private static final String CATEGORY_NAME = "Edukasi";
    private static final String READING_TITLE = "Belajar Java";
    private static final String READING_CONTENT = "Isi bacaan";
    private static final String QUESTION_TEXT = "Apa kepanjangan OOP?";
    private static final String OPTION_A = "Object Oriented Programming";
    private static final String OPTION_B = "Open Operational Protocol";

    @Mock
    private QuizQuestionRepository quizQuestionRepository;

    @Mock
    private QuizOptionRepository quizOptionRepository;

    @Mock
    private ReadingTextRepository readingTextRepository;

    @InjectMocks
    private QuizQuestionServiceImpl quizQuestionService;

    @Test
    void createQuestion_WhenRoleIsAdmin_ShouldSaveQuestion() {
        final Category category = new Category(1L, CATEGORY_NAME);
        final ReadingText readingText = new ReadingText(1L, READING_TITLE, READING_CONTENT, category);

        final QuizOptionRequest optionRequest1 = new QuizOptionRequest(OPTION_A, true);
        final QuizOptionRequest optionRequest2 = new QuizOptionRequest(OPTION_B, false);

        final QuizQuestionRequest request = new QuizQuestionRequest(
                QUESTION_TEXT,
                List.of(optionRequest1, optionRequest2)
        );

        final QuizQuestion savedQuestion = new QuizQuestion();
        savedQuestion.setId(10L);
        savedQuestion.setQuestionText(QUESTION_TEXT);
        savedQuestion.setReadingText(readingText);

        final QuizOption savedOption1 = new QuizOption();
        savedOption1.setId(100L);
        savedOption1.setOptionText(OPTION_A);
        savedOption1.setCorrect(true);
        savedOption1.setQuizQuestion(savedQuestion);

        final QuizOption savedOption2 = new QuizOption();
        savedOption2.setId(101L);
        savedOption2.setOptionText(OPTION_B);
        savedOption2.setCorrect(false);
        savedOption2.setQuizQuestion(savedQuestion);

        savedQuestion.setOptions(List.of(savedOption1, savedOption2));

        when(readingTextRepository.findById(1L)).thenReturn(Optional.of(readingText));
        when(quizQuestionRepository.save(any(QuizQuestion.class))).thenReturn(savedQuestion);

        final QuizQuestionResponse response =
                quizQuestionService.createQuestion(1L, request, ROLE_ADMIN);

        assertNotNull(response, "Response tidak boleh null");
        assertEquals(10L, response.id(), "ID question harus sesuai");
        assertEquals(QUESTION_TEXT, response.questionText(), "Question text harus sesuai");
        assertEquals(2, response.options().size(), "Jumlah option harus 2");
        verify(quizQuestionRepository, times(1)).save(any(QuizQuestion.class));
        verify(quizOptionRepository, times(2)).save(any(QuizOption.class));
    }

    @Test
    void createQuestion_WhenRoleIsPelajar_ShouldThrowException() {
        final QuizOptionRequest optionRequest = new QuizOptionRequest(OPTION_A, true);
        final QuizQuestionRequest request = new QuizQuestionRequest(
                QUESTION_TEXT,
                List.of(optionRequest)
        );

        assertThrows(
                RuntimeException.class,
                () -> quizQuestionService.createQuestion(1L, request, ROLE_PELAJAR),
                "Pelajar tidak boleh membuat question"
        );

        verify(quizQuestionRepository, never()).save(any(QuizQuestion.class));
        verify(quizOptionRepository, never()).save(any(QuizOption.class));
    }

    @Test
    void getQuestionsByReadingId_ShouldReturnQuestionList() {
        final Category category = new Category(1L, CATEGORY_NAME);
        final ReadingText readingText = new ReadingText(1L, READING_TITLE, READING_CONTENT, category);

        final QuizQuestion question = new QuizQuestion();
        question.setId(10L);
        question.setQuestionText(QUESTION_TEXT);
        question.setReadingText(readingText);

        final QuizOption option1 = new QuizOption();
        option1.setId(100L);
        option1.setOptionText(OPTION_A);
        option1.setCorrect(true);
        option1.setQuizQuestion(question);

        final QuizOption option2 = new QuizOption();
        option2.setId(101L);
        option2.setOptionText(OPTION_B);
        option2.setCorrect(false);
        option2.setQuizQuestion(question);

        question.setOptions(List.of(option1, option2));

        when(quizQuestionRepository.findByReadingTextId(1L)).thenReturn(List.of(question));

        final List<QuizQuestionResponse> responses = quizQuestionService.getQuestionsByReadingId(1L);

        assertNotNull(responses, "Response list tidak boleh null");
        assertEquals(1, responses.size(), "Jumlah question harus 1");
        assertEquals(QUESTION_TEXT, responses.get(0).questionText(), "Question text harus sesuai");
        assertEquals(2, responses.get(0).options().size(), "Jumlah option harus 2");
        verify(quizQuestionRepository, times(1)).findByReadingTextId(1L);
    }

    @Test
    void deleteQuestion_WhenRoleIsAdmin_ShouldDeleteQuestion() {
        when(quizQuestionRepository.existsById(10L)).thenReturn(true);

        quizQuestionService.deleteQuestion(10L, ROLE_ADMIN);

        verify(quizQuestionRepository, times(1)).deleteById(10L);
    }
}