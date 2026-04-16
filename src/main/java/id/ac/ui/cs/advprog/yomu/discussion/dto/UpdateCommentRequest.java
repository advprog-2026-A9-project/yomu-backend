package id.ac.ui.cs.advprog.yomu.discussion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UpdateCommentRequest {
    private String content;
    private UUID userId;
}