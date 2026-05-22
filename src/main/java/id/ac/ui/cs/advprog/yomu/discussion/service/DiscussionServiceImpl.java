package id.ac.ui.cs.advprog.yomu.discussion.service;

import id.ac.ui.cs.advprog.yomu.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.yomu.discussion.dto.*;
import id.ac.ui.cs.advprog.yomu.discussion.model.Comment;
import id.ac.ui.cs.advprog.yomu.discussion.model.CommentReaction;
import id.ac.ui.cs.advprog.yomu.discussion.model.ReactionType;
import id.ac.ui.cs.advprog.yomu.discussion.model.DiscussionUser;
import id.ac.ui.cs.advprog.yomu.discussion.repository.CommentReactionRepository;
import id.ac.ui.cs.advprog.yomu.discussion.repository.CommentRepository;
import id.ac.ui.cs.advprog.yomu.discussion.repository.DiscussionUserRepository;
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
    private final DiscussionUserRepository userRepository;
    
    private static final String COMMENT_NOT_FOUND = "Comment not found";
    private static final String UNAUTHORIZED_ACTION = "You are not authorized to %s this comment";

    private String resolveAuthorName1(final String userId) {
        return userRepository.findById(userId)
                .map(DiscussionUser::getUsername)
                .orElse("Unknown User");
    }
    
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
    public List<CommentResponse> getCommentsByReading(final Long readingId) { 
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
    public void deleteComment(final UUID commentId, final String userId) {
        final Comment comment = fetchCommentOrThrow(commentId);
        validateCommentOwnership(comment, userId, "delete");

        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public CommentResponse addReaction(final UUID commentId, final String userId, final ReactionRequest request) {
        fetchCommentOrThrow(commentId);

        final CommentReaction reaction = CommentReaction.builder()
                .commentId(commentId)
                .userId(userId)
                .type(request.getType())
                .emojiCode(request.getEmojiCode())
                .build();

        reactionRepository.save(reaction);
        return mapToResponse(fetchCommentOrThrow(commentId));
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

    private void validateCommentOwnership(final Comment comment, final String userId, final String action) {
        if (!comment.getUserId().equals(userId)) {
            throw new IllegalStateException(String.format(UNAUTHORIZED_ACTION, action));
        }
    }

    private CommentResponse mapToResponse(final Comment comment) {
        final List<CommentReaction> reactions = reactionRepository.findAllByCommentId(comment.getId());

        int upvotes = 0;
        int downvotes = 0;
        int fireReactions = 0;

        for (final CommentReaction reaction : reactions) {
            if (reaction.getType() == ReactionType.UPVOTE) {
                upvotes++;
            } else if (reaction.getType() == ReactionType.DOWNVOTE) {
                downvotes++;
            } else if (reaction.getType() == ReactionType.EMOJI && "🔥".equals(reaction.getEmojiCode())) {
                fireReactions++;
            }
        }

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUserId())
                .readingId(comment.getReadingId())
                .authorName(resolveAuthorName1(comment.getUserId()))
                .parentId(comment.getParentId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .upvotes(upvotes)
                .downvotes(downvotes)
                .fireReactions(fireReactions)
                .build();
    }

    private String resolveAuthorName(final String userId) {
        return userRepository.findById(userId)
                .map(user -> user.getUsername())
                .orElse("Unknown User");
    }

    @Override
    public List<CommentResponse> getAllComments() {
        return commentRepository.findAll().stream()
                .map(this::mapToResponse) 
                .toList();
    }
}