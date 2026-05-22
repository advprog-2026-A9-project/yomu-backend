package id.ac.ui.cs.advprog.yomu.discussion.repository;

import id.ac.ui.cs.advprog.yomu.discussion.model.CommentReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentReactionRepository extends JpaRepository<CommentReaction, UUID> {
    Optional<CommentReaction> findByCommentIdAndUserId(UUID commentId, String userId);
    void deleteAllByCommentId(UUID commentId);
    List<CommentReaction> findAllByCommentId(UUID commentId);
    List<CommentReaction> findByCommentIdIn(List<UUID> commentIds);
}