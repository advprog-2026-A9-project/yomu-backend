package id.ac.ui.cs.advprog.yomu.discussion.service;

import id.ac.ui.cs.advprog.yomu.discussion.dto.CommentResponse;
import id.ac.ui.cs.advprog.yomu.discussion.dto.CreateCommentRequest;
import id.ac.ui.cs.advprog.yomu.discussion.dto.UpdateCommentRequest;
import id.ac.ui.cs.advprog.yomu.discussion.model.Comment;
import id.ac.ui.cs.advprog.yomu.discussion.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscussionServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private DiscussionServiceImpl discussionService;

    private Comment mockComment;
    private UUID commentId;
    private UUID userId;
    private UUID otherUserId;
    private UUID readingId;
    private UUID parentId;

    @BeforeEach
    void setUp() {
        commentId = UUID.randomUUID();
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        readingId = UUID.randomUUID();
        parentId = UUID.randomUUID();

        mockComment = Comment.builder()
                .id(commentId)
                .content("Original content")
                .readingId(readingId)
                .userId(userId)
                .build();
    }

    @Test
    void testCreateNestedComment_Success() {
        CreateCommentRequest request = new CreateCommentRequest("Reply comment", readingId, userId, parentId);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentResponse response = discussionService.createComment(request);

        assertNotNull(response);
        assertEquals(parentId, response.getParentId()); // Memastikan threading berjalan
    }

    @Test
    void testUpdateComment_Success() {
        UpdateCommentRequest request = new UpdateCommentRequest("Updated content", userId);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);

        CommentResponse response = discussionService.updateComment(commentId, request);

        assertEquals("Updated content", response.getContent());
    }

    @Test
    void testUpdateComment_Unauthorized_ThrowsException() {
        UpdateCommentRequest request = new UpdateCommentRequest("Hacked content", otherUserId);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> 
            discussionService.updateComment(commentId, request)
        );
        assertEquals("You are not authorized to edit this comment", exception.getMessage());
    }

    @Test
    void testDeleteComment_Success() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        discussionService.deleteComment(commentId, userId);
        verify(commentRepository, times(1)).delete(mockComment);
    }

    @Test
    void testDeleteComment_Unauthorized_ThrowsException() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        assertThrows(IllegalStateException.class, () -> discussionService.deleteComment(commentId, otherUserId));
        verify(commentRepository, never()).delete(any(Comment.class));
    }
}