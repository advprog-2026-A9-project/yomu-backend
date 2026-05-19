package id.ac.ui.cs.advprog.yomu.gamification.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
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

    private Integer targetCount;

    private Integer accuracyThreshold;

    private Integer requiredCount;

    @NotNull(message = "Reward score is required")
    @Min(value = 1, message = "Reward score must be a strictly positive integer")
    private Integer rewardScore;

    private LocalDate activeFrom;

    private LocalDate activeUntil;
}
