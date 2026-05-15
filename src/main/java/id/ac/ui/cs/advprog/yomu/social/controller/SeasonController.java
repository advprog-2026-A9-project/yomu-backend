package id.ac.ui.cs.advprog.yomu.social.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import id.ac.ui.cs.advprog.yomu.social.dto.SeasonEndResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.SeasonStatusResponse;
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

    @GetMapping("/current")
    public ResponseEntity<SeasonStatusResponse> currentSeason() {
        return ResponseEntity.ok(seasonService.getCurrentSeason());
    }

    @PostMapping("/end")
    public ResponseEntity<SeasonEndResponse> endSeason() {
        String role = getRoleFromSecurityContext();
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(seasonService.endSeason());
    }
}
