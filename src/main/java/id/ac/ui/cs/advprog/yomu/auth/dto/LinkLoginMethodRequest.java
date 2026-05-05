package id.ac.ui.cs.advprog.yomu.auth.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter @Setter @NoArgsConstructor
public class LinkLoginMethodRequest {
    private String email;
    private String phoneNumber;
}