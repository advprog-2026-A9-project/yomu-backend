package id.ac.ui.cs.advprog.yomu.reading.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizAnswerRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizSubmissionRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizSubmissionResponse;
import id.ac.ui.cs.advprog.yomu.reading.service.QuizSubmissionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuizSubmissionController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
class QuizSubmissionControllerTest {

    private static final String USER_ID = "user-123";
    private static final Long TEXT_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QuizSubmissionService quizSubmissionService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private QuizSubmissionRequest validRequest;
    private QuizSubmissionResponse validResponse;

    @BeforeEach
    void setUp() {
        validRequest = new QuizSubmissionRequest(
                List.of(
                        new QuizAnswerRequest(10L, 100L),
                        new QuizAnswerRequest(20L, 201L)
                )
        );

        validResponse = new QuizSubmissionResponse(2, 1, 50, true);
    }

    // ==========================================
    // TEST SUBMIT QUIZ (POST)
    // ==========================================

    @Test
    @WithMockUser(username = USER_ID)
    void submitQuiz_WhenAuthorized_ShouldReturnOk() throws Exception {
        when(quizSubmissionService.submitQuiz(eq(TEXT_ID), eq(USER_ID), any(QuizSubmissionRequest.class)))
                .thenReturn(validResponse);

        mockMvc.perform(post("/api/reading-texts/{readingTextId}/quiz/submit", TEXT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuestions").value(2))
                .andExpect(jsonPath("$.correctAnswers").value(1))
                .andExpect(jsonPath("$.score").value(50))
                .andExpect(jsonPath("$.completed").value(true));

        verify(quizSubmissionService, times(1)).submitQuiz(eq(TEXT_ID), eq(USER_ID), any(QuizSubmissionRequest.class));
    }

    // ==========================================
    // TEST CHECK COMPLETION STATUS (GET)
    // ==========================================

    @Test
    @WithMockUser(username = USER_ID)
    void hasCompletedQuiz_WhenAuthorized_ShouldReturnOk() throws Exception {
        when(quizSubmissionService.hasCompletedQuiz(TEXT_ID, USER_ID)).thenReturn(true);

        mockMvc.perform(get("/api/reading-texts/{readingTextId}/quiz/completion", TEXT_ID))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(quizSubmissionService, times(1)).hasCompletedQuiz(TEXT_ID, USER_ID);
    }
}