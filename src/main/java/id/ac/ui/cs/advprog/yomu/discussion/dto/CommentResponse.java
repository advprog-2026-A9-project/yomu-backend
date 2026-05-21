package id.ac.ui.cs.advprog.yomu.discussion.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class CommentResponse {
    private UUID id;
    private String content;
    private String userId;
    private Long readingId;
    private String authorName;
    private UUID parentId; 
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt; 
    private int upvotes;
    private int downvotes;
    private int fireReactions;
}