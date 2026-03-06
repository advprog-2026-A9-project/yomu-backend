package id.ac.ui.cs.advprog.yomu.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class AuthResponse {
    private String userId;
    private String username;
    private String role;
    private String token;
    private String message;
}