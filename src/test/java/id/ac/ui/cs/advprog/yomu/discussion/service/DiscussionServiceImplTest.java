package id.ac.ui.cs.advprog.yomu.discussion.service;

import id.ac.ui.cs.advprog.yomu.auth.model.User;
import id.ac.ui.cs.advprog.yomu.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.yomu.discussion.dto.CommentResponse;
import id.ac.ui.cs.advprog.yomu.discussion.dto.CreateCommentRequest;
import id.ac.ui.cs.advprog.yomu.discussion.dto.UpdateCommentRequest;
import id.ac.ui.cs.advprog.yomu.discussion.dto.ReactionRequest;
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

import java.util.Collections;
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

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DiscussionServiceImpl discussionService;

    private Comment mockComment;
    private UUID commentId;
    private String userId;
    private String otherUserId;
    private Long readingId;
    private UUID parentId;

    @BeforeEach
    void setUp() {
        commentId = UUID.randomUUID();
        userId = UUID.randomUUID().toString();
        otherUserId = UUID.randomUUID().toString();
        readingId = 1L;
        parentId = UUID.randomUUID();

        mockComment = Comment.builder()
                .id(commentId)
                .content("Original content")
                .readingId(readingId)
                .userId(userId)
                .build();

        lenient().when(reactionRepository.findAllByCommentId(any(UUID.class))).thenReturn(Collections.emptyList());
        lenient().when(userRepository.findById(userId))
                .thenReturn(Optional.of(buildUser(userId, "testuser", "Test User")));
    }

    private User buildUser(String id, String username, String displayName) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setDisplayName(displayName);
        return user;
    }

    // --- PECAHAN TEST 1: Memeriksa Parent ID ---
    @Test
    void testCreateNestedCommentSetsParentId() {
        CreateCommentRequest request = new CreateCommentRequest("Reply comment", readingId, userId, parentId);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentResponse response = discussionService.createComment(request);
        assertEquals(parentId, response.getParentId(), "Parent ID must match to ensure threading works");
    }

    // --- PECAHAN TEST 2: Memeriksa Author Name ---
    @Test
    void testCreateNestedCommentSetsAuthorName() {
        CreateCommentRequest request = new CreateCommentRequest("Reply comment", readingId, userId, parentId);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentResponse response = discussionService.createComment(request);
        assertEquals("testuser", response.getAuthorName(), "Author name must match username");
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

        assertThrows(IllegalStateException.class, () -> discussionService.updateComment(commentId, request),
                "Must throw IllegalStateException if unauthorized");
    }

    @Test
    void testDeleteCommentSuccess() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        assertDoesNotThrow(() -> discussionService.deleteComment(commentId, userId),
                "Must not throw any exception on successful delete");
    }

    @Test
    void testDeleteCommentUnauthorizedThrowsException() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        assertThrows(IllegalStateException.class, () -> discussionService.deleteComment(commentId, otherUserId),
                "Must throw IllegalStateException if unauthorized");
    }

    // --- PECAHAN TEST 3: Memeriksa Data Count Reaksi ---
    @Test
    void testAddReactionIncrementsCount() {
        ReactionRequest request = new ReactionRequest();
        request.setType(ReactionType.UPVOTE);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        when(reactionRepository.save(any(CommentReaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reactionRepository.findAllByCommentId(commentId)).thenReturn(
                Collections.singletonList(CommentReaction.builder()
                        .commentId(commentId).userId(userId).type(ReactionType.UPVOTE).build())
        );

        CommentResponse response = discussionService.addReaction(commentId, userId, request);
        assertEquals(1, response.getUpvotes(), "Upvote count must increment after reaction");
    }

    // --- PECAHAN TEST 4: Memeriksa Pemanggilan Repository Reaksi ---
    @Test
    void testAddReactionSavesToRepository() {
        ReactionRequest request = new ReactionRequest();
        request.setType(ReactionType.UPVOTE);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        when(reactionRepository.save(any(CommentReaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        discussionService.addReaction(commentId, userId, request);
        verify(reactionRepository, times(1)).save(any(CommentReaction.class));
    }

    // --- PECAHAN TEST 5: Memeriksa Tidak Ada Exception pada Delete Admin ---
    @Test
    void testDeleteCommentByAdminDoesNotThrow() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        assertDoesNotThrow(() -> discussionService.deleteCommentByAdmin(commentId));
    }

    // --- PECAHAN TEST 6: Memeriksa Hapus Reaksi pada Delete Admin ---
    @Test
    void testDeleteCommentByAdminDeletesReactions() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        discussionService.deleteCommentByAdmin(commentId);
        verify(reactionRepository, times(1)).deleteAllByCommentId(commentId);
    }

    // --- PECAHAN TEST 7: Memeriksa Hapus Komentar pada Delete Admin ---
    @Test
    void testDeleteCommentByAdminDeletesComment() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        discussionService.deleteCommentByAdmin(commentId);
        verify(commentRepository, times(1)).delete(mockComment);
    }
}