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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReadingTextController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReadingTextControllerTest {

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
                "Judul Baru",
                "Isi Baru",
                1L
        );

        final ReadingTextResponse response = new ReadingTextResponse(
                1L,
                "Judul Baru",
                "Isi Baru",
                "Edukasi"
        );

        when(jwtUtil.extractRole("dummy-token")).thenReturn("ADMIN");
        when(readingTextService.createText(eq(request), eq("ADMIN"))).thenReturn(response);

        mockMvc.perform(post("/api/reading-texts")
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void getAllTexts_ShouldReturnOk() throws Exception {
        final List<ReadingTextResponse> responses = List.of(
                new ReadingTextResponse(1L, "Judul 1", "Isi 1", "Edukasi"),
                new ReadingTextResponse(2L, "Judul 2", "Isi 2", "Teknologi")
        );

        when(readingTextService.getAllTexts()).thenReturn(responses);

        mockMvc.perform(get("/api/reading-texts"))
                .andExpect(status().isOk());
    }

    @Test
    void getTextById_WhenTextExists_ShouldReturnOk() throws Exception {
        final ReadingTextResponse response = new ReadingTextResponse(
                1L,
                "Judul 1",
                "Isi 1",
                "Edukasi"
        );

        when(readingTextService.getTextById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/reading-texts/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteText_WhenAuthorized_ShouldReturnNoContent() throws Exception {
        when(jwtUtil.extractRole("dummy-token")).thenReturn("ADMIN");

        mockMvc.perform(delete("/api/reading-texts/1")
                        .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isNoContent());
    }
}