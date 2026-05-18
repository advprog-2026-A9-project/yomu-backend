package id.ac.ui.cs.advprog.yomu.reading.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import id.ac.ui.cs.advprog.yomu.reading.dto.ReadingTextRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.ReadingTextResponse;
import id.ac.ui.cs.advprog.yomu.reading.service.ReadingTextService;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReadingTextController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
class ReadingTextControllerTest {

    private static final String TITLE_JAVA = "Belajar Java";
    private static final String CONTENT_JAVA = "Isi teks tentang Java...";
    private static final String CATEGORY_NAME = "Edukasi";
    private static final Long CATEGORY_ID = 1L;
    private static final Long TEXT_ID = 10L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReadingTextService readingTextService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private ReadingTextRequest validRequest;
    private ReadingTextResponse validResponse;

    @BeforeEach
    void setUp() {
        validRequest = new ReadingTextRequest(TITLE_JAVA, CONTENT_JAVA, CATEGORY_ID);
        validResponse = new ReadingTextResponse(TEXT_ID, TITLE_JAVA, CONTENT_JAVA, CATEGORY_NAME);
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void createText_WhenAuthorized_ShouldReturnCreated() throws Exception {
        when(readingTextService.createText(any(ReadingTextRequest.class))).thenReturn(validResponse);

        mockMvc.perform(post("/api/reading-texts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TEXT_ID))
                .andExpect(jsonPath("$.title").value(TITLE_JAVA))
                .andExpect(jsonPath("$.content").value(CONTENT_JAVA))
                .andExpect(jsonPath("$.categoryName").value(CATEGORY_NAME));

        verify(readingTextService, times(1)).createText(any(ReadingTextRequest.class));
    }

    @Test
    @WithMockUser
    void getAllTexts_ShouldReturnOk() throws Exception {
        List<ReadingTextResponse> responses = List.of(validResponse);

        when(readingTextService.getAllTexts()).thenReturn(responses);

        mockMvc.perform(get("/api/reading-texts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].title").value(TITLE_JAVA))
                .andExpect(jsonPath("$[0].categoryName").value(CATEGORY_NAME));

        verify(readingTextService, times(1)).getAllTexts();
    }

    @Test
    @WithMockUser
    void getTextById_WhenTextExists_ShouldReturnOk() throws Exception {
        when(readingTextService.getTextById(TEXT_ID)).thenReturn(validResponse);

        mockMvc.perform(get("/api/reading-texts/" + TEXT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEXT_ID))
                .andExpect(jsonPath("$.title").value(TITLE_JAVA))
                .andExpect(jsonPath("$.categoryName").value(CATEGORY_NAME));

        verify(readingTextService, times(1)).getTextById(TEXT_ID);
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void updateText_WhenAuthorized_ShouldReturnOk() throws Exception {
        when(readingTextService.updateText(eq(TEXT_ID), any(ReadingTextRequest.class))).thenReturn(validResponse);

        mockMvc.perform(put("/api/reading-texts/" + TEXT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEXT_ID))
                .andExpect(jsonPath("$.title").value(TITLE_JAVA));

        verify(readingTextService, times(1)).updateText(eq(TEXT_ID), any(ReadingTextRequest.class));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void deleteText_WhenAuthorized_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/reading-texts/" + TEXT_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(readingTextService, times(1)).deleteText(TEXT_ID);
    }
}