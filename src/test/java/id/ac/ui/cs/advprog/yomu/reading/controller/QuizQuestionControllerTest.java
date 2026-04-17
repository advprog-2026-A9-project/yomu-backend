package id.ac.ui.cs.advprog.yomu.reading.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizOptionRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizOptionResponse;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizQuestionRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizQuestionResponse;
import id.ac.ui.cs.advprog.yomu.reading.service.QuizQuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuizQuestionController.class)
@AutoConfigureMockMvc(addFilters = false)
class QuizQuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuizQuestionService quizQuestionService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void createQuestion_WhenAuthorized_ShouldReturnCreated() throws Exception {
        final QuizQuestionRequest request = new QuizQuestionRequest(
                "Apa kepanjangan OOP?",
                List.of(
                        new QuizOptionRequest("Object Oriented Programming", true),
                        new QuizOptionRequest("Open Operational Protocol", false)
                )
        );

        final QuizQuestionResponse response = new QuizQuestionResponse(
                10L,
                "Apa kepanjangan OOP?",
                List.of(
                        new QuizOptionResponse(100L, "Object Oriented Programming"),
                        new QuizOptionResponse(101L, "Open Operational Protocol")
                )
        );

        when(jwtUtil.extractRole("dummy-token")).thenReturn("ADMIN");
        when(quizQuestionService.createQuestion(eq(1L), eq(request), eq("ADMIN"))).thenReturn(response);

        mockMvc.perform(post("/api/reading-texts/1/questions")
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void getQuestionsByReadingId_ShouldReturnOk() throws Exception {
        final List<QuizQuestionResponse> responses = List.of(
                new QuizQuestionResponse(
                        10L,
                        "Apa kepanjangan OOP?",
                        List.of(
                                new QuizOptionResponse(100L, "Object Oriented Programming"),
                                new QuizOptionResponse(101L, "Open Operational Protocol")
                        )
                )
        );

        when(quizQuestionService.getQuestionsByReadingId(1L)).thenReturn(responses);

        mockMvc.perform(get("/api/reading-texts/1/questions"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteQuestion_WhenAuthorized_ShouldReturnNoContent() throws Exception {
        when(jwtUtil.extractRole("dummy-token")).thenReturn("ADMIN");

        mockMvc.perform(delete("/api/reading-texts/1/questions/10")
                        .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isNoContent());
    }
}