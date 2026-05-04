package id.ac.ui.cs.advprog.yomu.discussion.service;

import id.ac.ui.cs.advprog.yomu.discussion.dto.CommentResponse;
import id.ac.ui.cs.advprog.yomu.discussion.dto.CreateCommentRequest;
import id.ac.ui.cs.advprog.yomu.discussion.dto.UpdateCommentRequest;
import id.ac.ui.cs.advprog.yomu.discussion.dto.ReactionRequest;
import static org.junit.jupiter.api.Assertions.assertAll;
import id.ac.ui.cs.advprog.yomu.discussion.model.Comment;
import id.ac.ui.cs.advprog.yomu.discussion.model.CommentReaction;
import id.ac.ui.cs.advprog.yomu.discussion.model.ReactionType;
import id.ac.ui.cs.advprog.yomu.discussion.repository.CommentRepository;
import id.ac.ui.cs.advprog.yomu.discussion.repository.CommentReactionRepository;
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

    @Mock
    private CommentReactionRepository reactionRepository;

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
    void testCreateNestedCommentSuccess() {
        CreateCommentRequest request = new CreateCommentRequest("Reply comment", readingId, userId, parentId);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentResponse response = discussionService.createComment(request);

        assertEquals(parentId, response.getParentId(), "Parent ID must match to ensure threading works"); 
    }

    @Test
    void testUpdateCommentSuccess() {
        UpdateCommentRequest request = new UpdateCommentRequest("Updated content", userId);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);

        CommentResponse response = discussionService.updateComment(commentId, request);

        assertEquals("Updated content", response.getContent(), "Comment content must be updated successfully");
    }

    @Test
    void testUpdateCommentUnauthorizedThrowsException() {
        UpdateCommentRequest request = new UpdateCommentRequest("Hacked content", otherUserId);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));

        assertThrows(IllegalStateException.class, () -> discussionService.updateComment(commentId, request), "Must throw IllegalStateException if unauthorized");
    }

    @Test
    void testDeleteCommentSuccess() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        
        assertDoesNotThrow(() -> discussionService.deleteComment(commentId, userId), "Must not throw any exception on successful delete");
    }

    @Test
    void testDeleteCommentUnauthorizedThrowsException() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        
        assertThrows(IllegalStateException.class, () -> discussionService.deleteComment(commentId, otherUserId), "Must throw IllegalStateException if unauthorized");
    }

    @Test
    void testAddReactionSuccess() {
        ReactionRequest request = new ReactionRequest();
        request.setType(ReactionType.UPVOTE);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        when(reactionRepository.findByCommentIdAndUserId(commentId, userId)).thenReturn(Optional.empty());

        assertAll("Add Reaction Success",
            () -> assertDoesNotThrow(() -> discussionService.addReaction(commentId, userId, request)),
            () -> verify(reactionRepository, times(1)).save(any(CommentReaction.class))
        );
    }

    @Test
    void testDeleteCommentByAdminSuccess() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));

        assertAll("Admin Moderation Success",
            () -> assertDoesNotThrow(() -> discussionService.deleteCommentByAdmin(commentId)),
            () -> verify(reactionRepository, times(1)).deleteAllByCommentId(commentId),
            () -> verify(commentRepository, times(1)).delete(mockComment)
        );
    }
}