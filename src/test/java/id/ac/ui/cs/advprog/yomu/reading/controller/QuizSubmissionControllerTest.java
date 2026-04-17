package id.ac.ui.cs.advprog.yomu.reading.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizAnswerRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizSubmissionRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizSubmissionResponse;
import id.ac.ui.cs.advprog.yomu.reading.service.QuizSubmissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
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

    private static final String DUMMY_TOKEN = "dummy-token";
    private static final String USER_ID = "user-123";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuizSubmissionService quizSubmissionService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void submitQuiz_WhenAuthorized_ShouldReturnOk() throws Exception {
        final QuizSubmissionRequest request = new QuizSubmissionRequest(
                List.of(
                        new QuizAnswerRequest(10L, 100L),
                        new QuizAnswerRequest(20L, 201L)
                )
        );

        final QuizSubmissionResponse response = new QuizSubmissionResponse(2, 1, 50, true);

        assertNotNull(request, "Request tidak boleh null");
        assertNotNull(response, "Response tidak boleh null");

        when(jwtUtil.extractUserId(DUMMY_TOKEN)).thenReturn(USER_ID);
        when(quizSubmissionService.submitQuiz(eq(1L), eq(USER_ID), eq(request))).thenReturn(response);

        mockMvc.perform(post("/api/reading-texts/1/quiz/submit")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuestions").value(2))
                .andExpect(jsonPath("$.correctAnswers").value(1))
                .andExpect(jsonPath("$.score").value(50))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void hasCompletedQuiz_WhenAuthorized_ShouldReturnOk() throws Exception {
        assertNotNull(DUMMY_TOKEN, "Token dummy tidak boleh null");
        assertNotNull(USER_ID, "User ID tidak boleh null");

        when(jwtUtil.extractUserId(DUMMY_TOKEN)).thenReturn(USER_ID);
        when(quizSubmissionService.hasCompletedQuiz(1L, USER_ID)).thenReturn(true);

        mockMvc.perform(get("/api/reading-texts/1/quiz/completion")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + DUMMY_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}