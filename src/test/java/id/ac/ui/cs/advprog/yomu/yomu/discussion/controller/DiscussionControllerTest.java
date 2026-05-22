package id.ac.ui.cs.advprog.yomu.discussion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.discussion.dto.CommentResponse;
import id.ac.ui.cs.advprog.yomu.discussion.dto.CreateCommentRequest;
import id.ac.ui.cs.advprog.yomu.discussion.dto.UpdateCommentRequest;
import id.ac.ui.cs.advprog.yomu.discussion.dto.ReactionRequest;
import id.ac.ui.cs.advprog.yomu.discussion.model.ReactionType;
import id.ac.ui.cs.advprog.yomu.discussion.service.DiscussionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import id.ac.ui.cs.advprog.yomu.auth.filter.JwtAuthenticationFilter;
import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import id.ac.ui.cs.advprog.yomu.auth.service.CustomOAuth2UserService;

import java.util.Collections;
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

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;

    private UUID commentId;
    private String userId;
    
    private static final String API_BASE_URL = "/api/discussion/";
    private static final String HTTP_200_MSG = "Must return HTTP 200 OK";
    private static final String HTTP_204_MSG = "Must return HTTP 204 No Content";

    @BeforeEach
    void setUp() {
        commentId = UUID.randomUUID();
        userId = UUID.randomUUID().toString();
    }

    @Test
    void testCreateCommentEndpoint() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("Test comment", 1L, userId, null);
        CommentResponse mockResponse = CommentResponse.builder().id(commentId).content("Test comment").userId(userId).build();

        when(discussionService.createComment(any(CreateCommentRequest.class))).thenReturn(mockResponse);

        int status = mockMvc.perform(post(API_BASE_URL + "create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getStatus();
                
        assertEquals(200, status, HTTP_200_MSG);
    }

    @Test
    void testGetCommentsByReadingEndpoint() throws Exception {
        Long readingId = 1L;
        when(discussionService.getCommentsByReading(readingId)).thenReturn(Collections.emptyList());

        int status = mockMvc.perform(get(API_BASE_URL + "reading/" + readingId))
                .andReturn().getResponse().getStatus();
                
        assertEquals(200, status, HTTP_200_MSG);
    }

    @Test
    void testUpdateCommentEndpoint() throws Exception {
        String updatedText = "Updated text";
        UpdateCommentRequest request = new UpdateCommentRequest(updatedText, userId);
        CommentResponse mockResponse = CommentResponse.builder().id(commentId).content(updatedText).userId(userId).build();

        when(discussionService.updateComment(eq(commentId), any(UpdateCommentRequest.class))).thenReturn(mockResponse);

        int status = mockMvc.perform(put(API_BASE_URL + commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getStatus();

        assertEquals(200, status, HTTP_200_MSG);
    }

    @Test
    void testDeleteCommentEndpoint() throws Exception {
        doNothing().when(discussionService).deleteComment(commentId, userId);

        int status = mockMvc.perform(delete(API_BASE_URL + commentId)
                .param("userId", userId))
                .andReturn().getResponse().getStatus();

        assertEquals(204, status, HTTP_204_MSG);
    }

    @Test
    void testGetAllCommentsAdminEndpoint() throws Exception {
        when(discussionService.getAllComments()).thenReturn(Collections.emptyList());

        int status = mockMvc.perform(get(API_BASE_URL + "all"))
                .andReturn().getResponse().getStatus();
                
        assertEquals(200, status, HTTP_200_MSG);
    }

    @Test
    void testAddReactionEndpoint() throws Exception {
        ReactionRequest request = new ReactionRequest();
        request.setType(ReactionType.UPVOTE);
        CommentResponse mockResponse = CommentResponse.builder().id(commentId).build();

        when(discussionService.addReaction(eq(commentId), eq(userId), any(ReactionRequest.class))).thenReturn(mockResponse);
        
        when(jwtUtil.extractUserId("mock-token")).thenReturn(userId);

        int status = mockMvc.perform(post(API_BASE_URL + commentId + "/reaction")
                .header("Authorization", "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getStatus();
                
        assertEquals(200, status, HTTP_200_MSG);
    }

    @Test
    void testModerateCommentAdminEndpoint() throws Exception {
        doNothing().when(discussionService).deleteCommentByAdmin(commentId);

        int status = mockMvc.perform(delete(API_BASE_URL + commentId + "/moderate"))
                .andReturn().getResponse().getStatus();
                
        assertEquals(204, status, HTTP_204_MSG);
    }

    @Test
    void testAddReactionEndpoint_WithoutBearer() throws Exception {
        ReactionRequest request = new ReactionRequest();
        request.setType(ReactionType.UPVOTE); 
        CommentResponse mockResponse = CommentResponse.builder().id(commentId).build();

        when(discussionService.addReaction(eq(commentId), eq(userId), any(ReactionRequest.class)))
                .thenReturn(mockResponse);
        
        when(jwtUtil.extractUserId("hanya-token-saja")).thenReturn(userId);

        int status = mockMvc.perform(post(API_BASE_URL + commentId + "/reaction")
                .header("Authorization", "hanya-token-saja") 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getStatus();
                
        assertEquals(200, status, HTTP_200_MSG);
    }
}