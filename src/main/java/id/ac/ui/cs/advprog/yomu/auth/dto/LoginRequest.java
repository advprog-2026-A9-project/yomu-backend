package id.ac.ui.cs.advprog.yomu.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginRequest {
    private String identifier; 
    private String password;
}