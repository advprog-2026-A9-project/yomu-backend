package id.ac.ui.cs.advprog.yomu.gamification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AchievementRequest {

    @NotBlank(message = "Achievement name is required")
    private String name;

    @NotBlank(message = "Achievement milestone is required")
    private String milestone;

    @NotBlank(message = "Milestone type is required")
    private String milestoneType;

    @jakarta.validation.constraints.Positive(message = "Milestone threshold must be a positive integer")
    private int milestoneThreshold;
}
