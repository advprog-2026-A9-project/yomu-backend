package id.ac.ui.cs.advprog.yomu.reading.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import id.ac.ui.cs.advprog.yomu.reading.dto.ReadingTextRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.ReadingTextResponse;
import id.ac.ui.cs.advprog.yomu.reading.service.ReadingTextService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReadingTextController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
class ReadingTextControllerTest {

    private static final String DUMMY_TOKEN = "dummy-token";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String TITLE_ONE = "Judul 1";
    private static final String TITLE_TWO = "Judul 2";
    private static final String CONTENT_ONE = "Isi 1";
    private static final String CONTENT_TWO = "Isi 2";
    private static final String CATEGORY_ONE = "Edukasi";
    private static final String CATEGORY_TWO = "Teknologi";
    private static final String NEW_TITLE = "Judul Baru";
    private static final String NEW_CONTENT = "Isi Baru";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReadingTextService readingTextService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void createText_WhenAuthorized_ShouldReturnCreated() throws Exception {
        final ReadingTextRequest request = new ReadingTextRequest(
                NEW_TITLE,
                NEW_CONTENT,
                1L
        );

        final ReadingTextResponse response = new ReadingTextResponse(
                1L,
                NEW_TITLE,
                NEW_CONTENT,
                CATEGORY_ONE
        );

        assertNotNull(request, "Request tidak boleh null");
        assertNotNull(response, "Response tidak boleh null");

        when(jwtUtil.extractRole(DUMMY_TOKEN)).thenReturn(ADMIN_ROLE);
        when(readingTextService.createText(eq(request), eq(ADMIN_ROLE))).thenReturn(response);

        mockMvc.perform(post("/api/reading-texts")
                        .header("Authorization", "Bearer " + DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value(NEW_TITLE));
    }

    @Test
    void getAllTexts_ShouldReturnOk() throws Exception {
        final List<ReadingTextResponse> responses = List.of(
                new ReadingTextResponse(1L, TITLE_ONE, CONTENT_ONE, CATEGORY_ONE),
                new ReadingTextResponse(2L, TITLE_TWO, CONTENT_TWO, CATEGORY_TWO)
        );

        assertNotNull(responses, "Responses tidak boleh null");

        when(readingTextService.getAllTexts()).thenReturn(responses);

        mockMvc.perform(get("/api/reading-texts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(TITLE_ONE))
                .andExpect(jsonPath("$[1].title").value(TITLE_TWO));
    }

    @Test
    void getTextById_WhenTextExists_ShouldReturnOk() throws Exception {
        final ReadingTextResponse response = new ReadingTextResponse(
                1L,
                TITLE_ONE,
                CONTENT_ONE,
                CATEGORY_ONE
        );

        assertNotNull(response, "Response tidak boleh null");

        when(readingTextService.getTextById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/reading-texts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value(TITLE_ONE));
    }

    @Test
    void deleteText_WhenAuthorized_ShouldReturnNoContent() throws Exception {
        assertNotNull(DUMMY_TOKEN, "Token dummy tidak boleh null");

        when(jwtUtil.extractRole(DUMMY_TOKEN)).thenReturn(ADMIN_ROLE);

        mockMvc.perform(delete("/api/reading-texts/1")
                        .header("Authorization", "Bearer " + DUMMY_TOKEN))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }
}