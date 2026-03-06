package id.ac.ui.cs.advprog.yomu.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequest {
    private String username;
    private String email;
    private String phoneNumber;
    private String displayName;
    private String password;
}