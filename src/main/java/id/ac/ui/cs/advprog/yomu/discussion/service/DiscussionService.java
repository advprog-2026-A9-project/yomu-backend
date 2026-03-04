package id.ac.ui.cs.advprog.yomu.discussion.service;

import id.ac.ui.cs.advprog.yomu.discussion.dto.CommentResponse;
import id.ac.ui.cs.advprog.yomu.discussion.dto.CreateCommentRequest;

import java.util.List;
import java.util.UUID;

public interface DiscussionService {
    CommentResponse createComment(CreateCommentRequest request);
    List<CommentResponse> getCommentsByReading(UUID readingId);
}