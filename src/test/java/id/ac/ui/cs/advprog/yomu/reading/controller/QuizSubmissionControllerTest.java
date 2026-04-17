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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuizSubmissionController.class)
@AutoConfigureMockMvc(addFilters = false)
class QuizSubmissionControllerTest {

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

        when(jwtUtil.extractUserId("dummy-token")).thenReturn("user-123");
        when(quizSubmissionService.submitQuiz(eq(1L), eq("user-123"), eq(request))).thenReturn(response);

        mockMvc.perform(post("/api/reading-texts/1/quiz/submit")
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void hasCompletedQuiz_WhenAuthorized_ShouldReturnOk() throws Exception {
        when(jwtUtil.extractUserId("dummy-token")).thenReturn("user-123");
        when(quizSubmissionService.hasCompletedQuiz(1L, "user-123")).thenReturn(true);

        mockMvc.perform(get("/api/reading-texts/1/quiz/completion")
                        .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}