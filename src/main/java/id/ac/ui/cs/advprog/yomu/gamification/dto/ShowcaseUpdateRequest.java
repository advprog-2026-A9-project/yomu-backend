package id.ac.ui.cs.advprog.yomu.gamification.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowcaseUpdateRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @Size(max = 3, message = "Showcase cannot exceed 3 achievements")
    private List<String> achievementIds;
}
