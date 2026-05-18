package id.ac.ui.cs.advprog.yomu.reading.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizOptionRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizOptionResponse;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizQuestionRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizQuestionResponse;
import id.ac.ui.cs.advprog.yomu.reading.service.QuizQuestionService;

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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuizQuestionController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
class QuizQuestionControllerTest {

    private static final String QUESTION_TEXT = "Apa kepanjangan OOP?";
    private static final String UPDATED_QUESTION_TEXT = "Apa fungsi utama OOP?"; // PMD Fix: Ekstraksi untuk update
    private static final String OPTION_ONE = "Object Oriented Programming";
    private static final String OPTION_TWO = "Open Operational Protocol";
    private static final Long TEXT_ID = 1L;
    private static final Long QUESTION_ID = 10L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QuizQuestionService quizQuestionService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private QuizQuestionRequest validRequest;
    private QuizQuestionResponse validResponse;

    @BeforeEach
    void setUp() {
        validRequest = new QuizQuestionRequest(
                QUESTION_TEXT,
                List.of(
                        new QuizOptionRequest(OPTION_ONE, true),
                        new QuizOptionRequest(OPTION_TWO, false)
                )
        );

        validResponse = new QuizQuestionResponse(
                QUESTION_ID,
                QUESTION_TEXT,
                List.of(
                        new QuizOptionResponse(100L, OPTION_ONE),
                        new QuizOptionResponse(101L, OPTION_TWO)
                )
        );
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void createQuestion_WhenAuthorized_ShouldReturnCreated() throws Exception {
        when(quizQuestionService.createQuestion(anyLong(), any(QuizQuestionRequest.class)))
                .thenReturn(validResponse);

        mockMvc.perform(post("/api/reading-texts/{readingTextId}/questions", TEXT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(QUESTION_ID))
                .andExpect(jsonPath("$.questionText").value(QUESTION_TEXT));

        verify(quizQuestionService, times(1)).createQuestion(eq(TEXT_ID), any(QuizQuestionRequest.class));
    }

    @Test
    @WithMockUser
    void getQuestionsByReadingId_ShouldReturnOk() throws Exception {
        when(quizQuestionService.getQuestionsByReadingId(TEXT_ID)).thenReturn(List.of(validResponse));

        mockMvc.perform(get("/api/reading-texts/{readingTextId}/questions", TEXT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(QUESTION_ID));

        verify(quizQuestionService, times(1)).getQuestionsByReadingId(TEXT_ID);
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void updateQuestion_WhenAuthorized_ShouldReturnOk() throws Exception {
        QuizQuestionRequest updateRequest = new QuizQuestionRequest(
                UPDATED_QUESTION_TEXT,
                List.of(new QuizOptionRequest(OPTION_ONE, true))
        );

        QuizQuestionResponse updatedResponse = new QuizQuestionResponse(
                QUESTION_ID,
                UPDATED_QUESTION_TEXT,
                List.of(new QuizOptionResponse(100L, OPTION_ONE))
        );

        when(quizQuestionService.updateQuestion(eq(QUESTION_ID), any(QuizQuestionRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/reading-texts/{readingTextId}/questions/{questionId}", TEXT_ID, QUESTION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(QUESTION_ID))
                .andExpect(jsonPath("$.questionText").value(UPDATED_QUESTION_TEXT));

        verify(quizQuestionService, times(1)).updateQuestion(eq(QUESTION_ID), any(QuizQuestionRequest.class));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void deleteQuestion_WhenAuthorized_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/reading-texts/{readingTextId}/questions/{questionId}", TEXT_ID, QUESTION_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(quizQuestionService, times(1)).deleteQuestion(QUESTION_ID);
    }
}