package id.ac.ui.cs.advprog.yomu.discussion.service;

import id.ac.ui.cs.advprog.yomu.discussion.dto.CommentResponse;
import id.ac.ui.cs.advprog.yomu.discussion.dto.CreateCommentRequest;
import id.ac.ui.cs.advprog.yomu.discussion.dto.ReactionRequest;
import id.ac.ui.cs.advprog.yomu.discussion.dto.UpdateCommentRequest;

import java.util.List;
import java.util.UUID;

public interface DiscussionService {
    CommentResponse createComment(CreateCommentRequest request);
    List<CommentResponse> getCommentsByReading(Long readingId);
    CommentResponse updateComment(UUID commentId, UpdateCommentRequest request);
    void deleteComment(UUID commentId, String userId);
    List<CommentResponse> getAllComments();
    CommentResponse addReaction(UUID commentId, String userId, ReactionRequest request);
    void deleteCommentByAdmin(UUID commentId);
}