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
    private static final String UNAUTHORIZED_ACTION = "You are not authorized to %s this comment";

    @Override
    @Transactional
    public CommentResponse createComment(final CreateCommentRequest request) {
        final Comment comment = Comment.builder()
                .content(request.getContent())
                .readingId(request.getReadingId())
                .userId(request.getUserId())
                .parentId(request.getParentId())
                .build();
        return mapToResponse(commentRepository.save(comment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByReading(final UUID readingId) {
        return commentRepository.findByReadingId(readingId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponse updateComment(final UUID commentId, final UpdateCommentRequest request) {
        final Comment comment = fetchCommentOrThrow(commentId);
        validateCommentOwnership(comment, request.getUserId(), "edit");

        comment.setContent(request.getContent());
        return mapToResponse(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(final UUID commentId, final UUID userId) {
        final Comment comment = fetchCommentOrThrow(commentId);
        validateCommentOwnership(comment, userId, "delete");

        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public void addReaction(final UUID commentId, final UUID userId, final ReactionRequest request) {
        final Comment comment = fetchCommentOrThrow(commentId);

        final CommentReaction reaction = reactionRepository.findByCommentIdAndUserId(commentId, userId)
                .orElse(new CommentReaction());

        reaction.setCommentId(comment.getId());
        reaction.setUserId(userId);
        reaction.setType(request.getType());
        reaction.setEmojiCode(request.getEmojiCode());

        reactionRepository.save(reaction);
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(final UUID commentId) {
        final Comment comment = fetchCommentOrThrow(commentId);
        reactionRepository.deleteAllByCommentId(commentId);
        commentRepository.delete(comment);
    }

    private Comment fetchCommentOrThrow(final UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException(COMMENT_NOT_FOUND));
    }

    private void validateCommentOwnership(final Comment comment, final UUID userId, final String action) {
        if (!comment.getUserId().equals(userId)) {
            throw new IllegalStateException(String.format(UNAUTHORIZED_ACTION, action));
        }
    }

    private CommentResponse mapToResponse(final Comment comment) {
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
}