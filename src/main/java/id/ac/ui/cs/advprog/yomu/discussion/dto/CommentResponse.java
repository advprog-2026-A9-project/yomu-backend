package id.ac.ui.cs.advprog.yomu.discussion.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class CommentResponse {
    private UUID id;
    private String content;
    private UUID userId;
    private UUID readingId;
    private UUID parentId; 
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt; 
    private long upvotes;
    private long downvotes;
    private long fireReactions;
}