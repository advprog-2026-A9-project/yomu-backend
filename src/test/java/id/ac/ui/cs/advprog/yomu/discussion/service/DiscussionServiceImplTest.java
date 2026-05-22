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
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import id.ac.ui.cs.advprog.yomu.discussion.repository.CommentReactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscussionServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentReactionRepository reactionRepository;

    @Mock
    private UserRepository userRepository;

    
    private DiscussionServiceImpl discussionService;

    
    @Mock
    private MeterRegistry meterRegistry; 


    @Mock
    private Counter counter;

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

        lenient().when(meterRegistry.counter(anyString())).thenReturn(counter);

        discussionService = new DiscussionServiceImpl(commentRepository, reactionRepository, userRepository, meterRegistry);

        lenient().when(reactionRepository.findAllByCommentId(any(UUID.class))).thenReturn(Collections.emptyList());
        lenient().when(userRepository.findById(userId)).thenReturn(Optional.of(buildUser(userId, "testuser", "Test User")));
    }

    private User buildUser(String id, String username, String displayName) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setDisplayName(displayName);
        return user;
    }

    @Test
    void testCreateCommentSuccess() {
        CreateCommentRequest request = new CreateCommentRequest("Reply comment", readingId, userId, parentId);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentResponse response = discussionService.createComment(request);
        
        assertAll("Verify created comment response",
            () -> assertNotNull(response, "Response should not be null"),
            () -> assertEquals(parentId, response.getParentId(), "Parent ID must match the requested parentId"),
            () -> assertEquals("testuser", response.getAuthorName(), "Author name must match the mocked user's username")
        );
    }

    @Test
    void testGetCommentsByReading() {
        when(commentRepository.findByReadingId(readingId))
                .thenReturn(Collections.singletonList(mockComment));

        List<CommentResponse> result = discussionService.getCommentsByReading(readingId);
        
        assertAll("Verify get comments by reading",
            () -> assertFalse(result.isEmpty(), "Result list should not be empty"),
            () -> assertEquals(readingId, result.get(0).getReadingId(), "Reading ID of the fetched comment must match")
        );
    }

    @Test
    void testUpdateCommentSuccess() {
        UpdateCommentRequest request = new UpdateCommentRequest("Updated content", userId);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);

        CommentResponse response = discussionService.updateComment(commentId, request);
        assertEquals("Updated content", response.getContent(), "Comment content should be updated to the requested text");
    }

    @Test
    void testUpdateCommentUnauthorizedThrowsException() {
        UpdateCommentRequest request = new UpdateCommentRequest("Hacked content", otherUserId);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));

        assertThrows(IllegalStateException.class, () -> discussionService.updateComment(commentId, request), "Updating a comment with a different userId should throw IllegalStateException");
    }

    @Test
    void testDeleteCommentSuccess() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        assertDoesNotThrow(() -> discussionService.deleteComment(commentId, userId), "Deleting an owned comment should not throw any exceptions");
    }

    @Test
    void testDeleteCommentUnauthorizedThrowsException() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        assertThrows(IllegalStateException.class, () -> discussionService.deleteComment(commentId, otherUserId), "Deleting a comment with a different userId should throw IllegalStateException");
    }

    @Test
    void testGetAllComments() {
        when(commentRepository.findAll()).thenReturn(Collections.singletonList(mockComment));

        List<CommentResponse> result = discussionService.getAllComments();
        assertFalse(result.isEmpty(), "Result list should not be empty when comments exist");
    }

    @Test
    void testAddReactionSuccess() {
        ReactionRequest request = new ReactionRequest();
        request.setType(ReactionType.UPVOTE);
        
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        when(reactionRepository.save(any(CommentReaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reactionRepository.findAllByCommentId(commentId)).thenReturn(
                Collections.singletonList(CommentReaction.builder().commentId(commentId).userId(userId).type(ReactionType.UPVOTE).build())
        );

        CommentResponse response = discussionService.addReaction(commentId, userId, request);
        
        assertAll("Verify upvote reaction added",
            () -> assertEquals(1, response.getUpvotes(), "Upvotes count should increment to 1"),
            () -> verify(reactionRepository, times(1)).save(any(CommentReaction.class))
        );
    }
    
    @Test
    void testAddReactionDownVoteSuccess() {
        ReactionRequest request = new ReactionRequest();
        request.setType(ReactionType.DOWNVOTE);
        
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        when(reactionRepository.save(any(CommentReaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reactionRepository.findAllByCommentId(commentId)).thenReturn(
                Collections.singletonList(CommentReaction.builder()
                        .commentId(commentId).userId(userId).type(ReactionType.DOWNVOTE).build())
        );

        CommentResponse response = discussionService.addReaction(commentId, userId, request);
        
        assertAll("Verify downvote reaction added",
            () -> assertEquals(1, response.getDownvotes(), "Downvotes count should increment to 1"),
            () -> verify(reactionRepository, times(1)).save(any(CommentReaction.class))
        );
    }
    
    @Test
    void testAddReactionFireSuccess() {
        ReactionRequest request = new ReactionRequest();
        request.setType(ReactionType.EMOJI);
        
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        when(reactionRepository.save(any(CommentReaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reactionRepository.findAllByCommentId(commentId)).thenReturn(
                Collections.singletonList(CommentReaction.builder()
                        .commentId(commentId).userId(userId).type(ReactionType.EMOJI).emojiCode("🔥").build())
        );

        CommentResponse response = discussionService.addReaction(commentId, userId, request);
        
        assertAll("Verify fire emoji reaction added",
            () -> assertEquals(1, response.getFireReactions(), "Fire reactions count should increment to 1"),
            () -> verify(reactionRepository, times(1)).save(any(CommentReaction.class))
        );
    }

    @Test
    void testDeleteCommentByAdminSuccess() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));
        
        assertAll("Verify admin deletion does not throw and calls repositories",
            () -> assertDoesNotThrow(() -> discussionService.deleteCommentByAdmin(commentId), "Admin deletion should succeed without exceptions"),
            () -> verify(reactionRepository, times(1)).deleteAllByCommentId(commentId),
            () -> verify(commentRepository, times(1)).delete(mockComment)
        );
    }

    @Test
    void testFetchCommentOrThrowNotFound() {
        UUID randomId = UUID.randomUUID();
        when(commentRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> discussionService.deleteCommentByAdmin(randomId), "Fetching a non-existent comment should throw IllegalArgumentException");
    }

}