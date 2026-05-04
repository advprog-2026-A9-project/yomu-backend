package id.ac.ui.cs.advprog.yomu.discussion.service;

import id.ac.ui.cs.advprog.yomu.discussion.dto.*;
import id.ac.ui.cs.advprog.yomu.discussion.model.Comment;
import id.ac.ui.cs.advprog.yomu.discussion.model.CommentReaction;
import id.ac.ui.cs.advprog.yomu.discussion.repository.CommentReactionRepository;
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

    private final CommentReactionRepository reactionRepository;

    private static final String COMMENT_NOT_FOUND = "Comment not found";

    @Override
    @Transactional
    public CommentResponse createComment(CreateCommentRequest request) {
        Comment comment = Comment.builder()
                .content(request.getContent())
                .readingId(request.getReadingId())
                .userId(request.getUserId())
                .parentId(request.getParentId())
                .build();
        return mapToResponse(commentRepository.save(comment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByReading(UUID readingId) {
        return commentRepository.findByReadingId(readingId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponse updateComment(UUID commentId, UpdateCommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException(COMMENT_NOT_FOUND));

        if (!comment.getUserId().equals(request.getUserId())) {
            throw new IllegalStateException("You are not authorized to edit this comment");
        }

        comment.setContent(request.getContent());
        return mapToResponse(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException(COMMENT_NOT_FOUND));

        if (!comment.getUserId().equals(userId)) {
            throw new IllegalStateException("You are not authorized to delete this comment");
        }

        commentRepository.delete(comment);
    }

    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUserId())
                .readingId(comment.getReadingId())
                .parentId(comment.getParentId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public void addReaction(UUID commentId, UUID userId, ReactionRequest request) {
        final Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException(COMMENT_NOT_FOUND));

        CommentReaction reaction = reactionRepository.findByCommentIdAndUserId(commentId, userId)
                .orElse(new CommentReaction());

        reaction.setCommentId(comment.getId());
        reaction.setUserId(userId);
        reaction.setType(request.getType());
        reaction.setEmojiCode(request.getEmojiCode());

        reactionRepository.save(reaction);
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(UUID commentId) {
        final Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException(COMMENT_NOT_FOUND));

        reactionRepository.deleteAllByCommentId(commentId);
        commentRepository.delete(comment);

    }
}