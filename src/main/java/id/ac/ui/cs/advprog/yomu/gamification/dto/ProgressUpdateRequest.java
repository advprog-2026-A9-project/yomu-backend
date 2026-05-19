package id.ac.ui.cs.advprog.yomu.gamification.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgressUpdateRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Master data ID is required")
    private String masterId;

    @Min(value = 0, message = "Progress value must be >= 0")
    private int progressValue;
}
