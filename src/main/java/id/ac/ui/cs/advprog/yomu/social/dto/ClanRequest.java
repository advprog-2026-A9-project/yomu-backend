package id.ac.ui.cs.advprog.yomu.social.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClanRequest {

    @NotBlank(message = "Clan name cannot be empty")
    @Size(max = 100, message = "Clan name exceeds maximum length of 100")
    private String name;

    @Size(max = 255, message = "Clan description exceeds maximum length of 255")
    private String description;

    private String username;
}