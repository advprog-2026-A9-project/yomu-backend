package id.ac.ui.cs.advprog.yomu.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccountResponse {
    private String id;
    private String username;
    private String displayName;
    private String email;
    private String phoneNumber;
    private String role;
    private String message;
}