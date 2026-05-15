package id.ac.ui.cs.advprog.yomu.gamification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DailyMissionRequest {

    @NotBlank(message = "Daily mission name is required")
    private String name;

    @NotBlank(message = "Daily mission milestone is required")
    private String milestone;

    @NotBlank(message = "Mission type is required")
    private String missionType;

    @jakarta.validation.constraints.Positive(message = "Target count must be a positive integer")
    private int targetCount;

    @NotBlank(message = "Reward description is required")
    private String rewardDescription;

    private java.time.LocalDate activeFrom;

    private java.time.LocalDate activeUntil;
}
