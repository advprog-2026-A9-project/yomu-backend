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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuizQuestionController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
class QuizQuestionControllerTest {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String QUESTION_TEXT = "Apa kepanjangan OOP?";
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
        // Setup DRY: Mengurangi repetisi pembuatan List Option di setiap Test
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

    // ==========================================
    // TEST CREATE QUESTION (POST)
    // ==========================================

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"}) // Simulasi Security Context
    void createQuestion_WhenAuthorized_ShouldReturnCreated() throws Exception {
        when(quizQuestionService.createQuestion(anyLong(), any(QuizQuestionRequest.class), anyString()))
                .thenReturn(validResponse);

        // Gunakan URI Variables agar lebih mudah diganti jika ada perubahan path
        mockMvc.perform(post("/api/reading-texts/{readingTextId}/questions", TEXT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(QUESTION_ID))
                .andExpect(jsonPath("$.questionText").value(QUESTION_TEXT))
                .andExpect(jsonPath("$.options.size()").value(2)) // Verifikasi Nested JSON Array
                .andExpect(jsonPath("$.options[0].optionText").value(OPTION_ONE));

        verify(quizQuestionService, times(1)).createQuestion(eq(TEXT_ID), any(QuizQuestionRequest.class), anyString());
    }

    // ==========================================
    // TEST GET QUESTIONS (GET)
    // ==========================================

    @Test
    @WithMockUser
    void getQuestionsByReadingId_ShouldReturnOk() throws Exception {
        when(quizQuestionService.getQuestionsByReadingId(TEXT_ID)).thenReturn(List.of(validResponse));

        mockMvc.perform(get("/api/reading-texts/{readingTextId}/questions", TEXT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(QUESTION_ID))
                .andExpect(jsonPath("$[0].questionText").value(QUESTION_TEXT))
                .andExpect(jsonPath("$[0].options.size()").value(2));

        verify(quizQuestionService, times(1)).getQuestionsByReadingId(TEXT_ID);
    }

    // ==========================================
    // TEST DELETE QUESTION (DELETE)
    // ==========================================

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void deleteQuestion_WhenAuthorized_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/reading-texts/{readingTextId}/questions/{questionId}", TEXT_ID, QUESTION_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(quizQuestionService, times(1)).deleteQuestion(eq(QUESTION_ID), anyString());
    }
}