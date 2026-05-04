package id.ac.ui.cs.advprog.yomu.reading.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizOptionRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizOptionResponse;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizQuestionRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizQuestionResponse;
import id.ac.ui.cs.advprog.yomu.reading.service.QuizQuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuizQuestionService quizQuestionService;

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"}) // Simulasi Security Context
    void createQuestion_WhenAuthorized_ShouldReturnCreated() throws Exception {
        final QuizQuestionRequest request = new QuizQuestionRequest(
                QUESTION_TEXT,
                List.of(
                        new QuizOptionRequest(OPTION_ONE, true),
                        new QuizOptionRequest(OPTION_TWO, false)
                )
        );

        final QuizQuestionResponse response = new QuizQuestionResponse(
                10L,
                QUESTION_TEXT,
                List.of(
                        new QuizOptionResponse(100L, OPTION_ONE),
                        new QuizOptionResponse(101L, OPTION_TWO)
                )
        );

        assertNotNull(request, "Request tidak boleh null");
        assertNotNull(response, "Response tidak boleh null");

        when(quizQuestionService.createQuestion(eq(1L), eq(request), eq(ADMIN_ROLE))).thenReturn(response);

        mockMvc.perform(post("/api/reading-texts/1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.questionText").value(QUESTION_TEXT));
    }

    @Test
    @WithMockUser
    void getQuestionsByReadingId_ShouldReturnOk() throws Exception {
        final List<QuizQuestionResponse> responses = List.of(
                new QuizQuestionResponse(
                        10L,
                        QUESTION_TEXT,
                        List.of(
                                new QuizOptionResponse(100L, OPTION_ONE),
                                new QuizOptionResponse(101L, OPTION_TWO)
                        )
                )
        );

        assertNotNull(responses, "Responses tidak boleh null");

        when(quizQuestionService.getQuestionsByReadingId(1L)).thenReturn(responses);

        mockMvc.perform(get("/api/reading-texts/1/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].questionText").value(QUESTION_TEXT));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void deleteQuestion_WhenAuthorized_ShouldReturnNoContent() throws Exception {
        var result = mockMvc.perform(delete("/api/reading-texts/1/questions/10"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andReturn();

        assertNotNull(result, "Result tidak boleh null");
    }
}