package id.ac.ui.cs.advprog.yomu.discussion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.discussion.dto.CommentResponse;
import id.ac.ui.cs.advprog.yomu.discussion.dto.UpdateCommentRequest;
import id.ac.ui.cs.advprog.yomu.discussion.service.DiscussionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(controllers = DiscussionController.class)
@AutoConfigureMockMvc(addFilters = false)
class DiscussionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DiscussionService discussionService;

    private UUID commentId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        commentId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void testUpdateCommentEndpoint() throws Exception {
        String updatedText = "Updated text";
        UpdateCommentRequest request = new UpdateCommentRequest(updatedText, userId);
        CommentResponse mockResponse = CommentResponse.builder()
                .id(commentId)
                .content(updatedText)
                .userId(userId)
                .build();

        when(discussionService.updateComment(eq(commentId), any(UpdateCommentRequest.class)))
                .thenReturn(mockResponse);

        // Menangkap response status untuk di-assert secara eksplisit
        int status = mockMvc.perform(put("/api/discussion/" + commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getStatus();

        // Menyertakan message dan assertion eksplisit
        assertEquals(200, status, "Must return HTTP 200 OK");
    }

    @Test
    void testDeleteCommentEndpoint() throws Exception {
        // Menangkap response status untuk di-assert secara eksplisit
        int status = mockMvc.perform(delete("/api/discussion/" + commentId)
                .param("userId", userId.toString()))
                .andReturn().getResponse().getStatus();

        // Menyertakan message dan assertion eksplisit
        assertEquals(204, status, "Must return HTTP 204 No Content");
    }
}