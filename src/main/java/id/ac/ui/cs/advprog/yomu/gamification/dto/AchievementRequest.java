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

    @jakarta.validation.constraints.Min(value = 1, message = "Milestone threshold must be positive")
    private int milestoneThreshold;

    private String tier;

    private String targetTier;

    private Integer accuracyThreshold;
}
