package id.ac.ui.cs.advprog.yomu.discussion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UpdateCommentRequest {
    private String content;
    private String userId;
}