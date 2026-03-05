package id.ac.ui.cs.advprog.yomu.discussion.Service;
    
import id.ac.ui.cs.advprog.yomu.discussion.service.DiscussionServiceImpl;
import id.ac.ui.cs.advprog.yomu.discussion.dto.CommentResponse;
import id.ac.ui.cs.advprog.yomu.discussion.dto.CreateCommentRequest;
import id.ac.ui.cs.advprog.yomu.discussion.model.Comment;
import id.ac.ui.cs.advprog.yomu.discussion.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
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

    private CreateCommentRequest createRequest;
    private Comment comment;
    private UUID readingId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        readingId = UUID.randomUUID();
        userId = UUID.randomUUID();

        createRequest = CreateCommentRequest.builder()
                .content("Bagus sekali!")
                .readingId(readingId)
                .userId(userId)
                .build();

        comment = Comment.builder()
                .id(UUID.randomUUID())
                .content("Bagus sekali!")
                .readingId(readingId)
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // --- PECAHAN TEST CREATE COMMENT ---

    @Test
    void testCreateComment_Success_ReturnsNotNull() {
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        CommentResponse response = discussionService.createComment(createRequest);
        
        // Hanya 1 assert
        assertNotNull(response, "Response tidak boleh null");
    }

    @Test
    void testCreateComment_Success_ReturnsCorrectContent() {
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        CommentResponse response = discussionService.createComment(createRequest);
        
        // Hanya 1 assert
        assertEquals("Bagus sekali!", response.getContent(), "Konten komentar harus sesuai");
    }

    @Test
    void testCreateComment_Success_ReturnsCorrectReadingId() {
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        CommentResponse response = discussionService.createComment(createRequest);
        
        // Hanya 1 assert
        assertEquals(readingId, response.getReadingId(), "Reading ID harus sesuai");
    }

    // --- PECAHAN TEST EXCEPTION ---

    @Test
    void testCreateComment_EmptyContent_ShouldThrowException() {
        createRequest.setContent("");
        
        // assertThrows dihitung sebagai 1 assert, dan pesannya sudah lengkap
        assertThrows(IllegalArgumentException.class, () -> {
            discussionService.createComment(createRequest);
        }, "Harusnya melempar IllegalArgumentException jika konten kosong");
    }

    // --- PECAHAN TEST GET COMMENTS ---

    @Test
    void testGetCommentsByReading_ReturnsCorrectSize() {
        when(commentRepository.findByReadingId(readingId)).thenReturn(List.of(comment));
        List<CommentResponse> responses = discussionService.getCommentsByReading(readingId);
        
        // Hanya 1 assert
        assertEquals(1, responses.size(), "Ukuran list harus 1");
    }

    @Test
    void testGetCommentsByReading_ReturnsCorrectContent() {
        when(commentRepository.findByReadingId(readingId)).thenReturn(List.of(comment));
        List<CommentResponse> responses = discussionService.getCommentsByReading(readingId);
        
        // Hanya 1 assert
        assertEquals(comment.getContent(), responses.get(0).getContent(), "Konten komentar pertama harus sesuai");
    }
}