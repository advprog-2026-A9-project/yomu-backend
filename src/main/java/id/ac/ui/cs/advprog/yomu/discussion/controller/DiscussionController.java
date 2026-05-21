package id.ac.ui.cs.advprog.yomu.discussion.controller;

import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import id.ac.ui.cs.advprog.yomu.discussion.dto.CommentResponse;
import id.ac.ui.cs.advprog.yomu.discussion.dto.CreateCommentRequest;
import id.ac.ui.cs.advprog.yomu.discussion.dto.UpdateCommentRequest;
import id.ac.ui.cs.advprog.yomu.discussion.service.DiscussionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import id.ac.ui.cs.advprog.yomu.discussion.dto.ReactionRequest;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/discussion")
@RequiredArgsConstructor
public class DiscussionController {

    private final DiscussionService discussionService;
    private final JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<CommentResponse> createComment(@RequestBody CreateCommentRequest request) {
        return ResponseEntity.ok(discussionService.createComment(request));
    }

    @GetMapping("/reading/{readingId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByReading(@PathVariable Long readingId) {
        return ResponseEntity.ok(discussionService.getCommentsByReading(readingId));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable UUID commentId,
            @RequestBody UpdateCommentRequest request) {
        return ResponseEntity.ok(discussionService.updateComment(commentId, request));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID commentId,
            @RequestParam String userId) {
        discussionService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{commentId}/reaction")
    public ResponseEntity<CommentResponse> addReaction(
            @PathVariable UUID commentId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ReactionRequest request) {
        final String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        final String userId = jwtUtil.extractUserId(token);
        return ResponseEntity.ok(discussionService.addReaction(commentId, userId, request));
    }

    @DeleteMapping("/{commentId}/moderate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> moderateCommentAdmin(@PathVariable UUID commentId) {
        discussionService.deleteCommentByAdmin(commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<CommentResponse>> getAllCommentsAdmin() {
        List<CommentResponse> allComments = discussionService.getAllComments();
        return ResponseEntity.ok(allComments);
    }
}
