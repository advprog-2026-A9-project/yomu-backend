package id.ac.ui.cs.advprog.yomu.social.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.service.SeasonService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/seasons")
@RequiredArgsConstructor
public class SeasonController {

    private final SeasonService seasonService;

    private String getRoleFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null
                && !authentication.getAuthorities().isEmpty()) {
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            return role.startsWith("ROLE_") ? role.substring(5) : role;
        }
        return "PELAJAR";
    }

    @PostMapping("/end")
    public ResponseEntity<String> endSeason() {
        String role = getRoleFromSecurityContext();
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        seasonService.endSeason();
        return ResponseEntity.ok(SocialConstants.END_SEASON_SUCCESS_MESSAGE);
    }
}
