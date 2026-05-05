package id.ac.ui.cs.advprog.yomu.discussion.dto;

import id.ac.ui.cs.advprog.yomu.discussion.model.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionRequest {
    private ReactionType type;
    private String emojiCode;
}