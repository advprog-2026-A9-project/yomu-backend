package id.ac.ui.cs.advprog.yomu.auth.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter @Setter @NoArgsConstructor
public class UpdateAccountRequest {
    private String displayName;
    private String oldPassword;
    private String newPassword;
}