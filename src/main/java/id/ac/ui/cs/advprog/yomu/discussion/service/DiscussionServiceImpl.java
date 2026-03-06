package id.ac.ui.cs.advprog.yomu.discussion.service;

import id.ac.ui.cs.advprog.yomu.discussion.dto.CommentResponse;
import id.ac.ui.cs.advprog.yomu.discussion.dto.CreateCommentRequest;
import id.ac.ui.cs.advprog.yomu.discussion.model.Comment;
import id.ac.ui.cs.advprog.yomu.discussion.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscussionServiceImpl implements DiscussionService {

    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public CommentResponse createComment(CreateCommentRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }

        Comment comment = Comment.builder()
                .content(request.getContent())
                .readingId(request.getReadingId())
                .userId(request.getUserId())
                .build();

        Comment savedComment = commentRepository.save(comment);
        return mapToResponse(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByReading(UUID readingId) {
        return commentRepository.findByReadingId(readingId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUserId())
                .readingId(comment.getReadingId())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}