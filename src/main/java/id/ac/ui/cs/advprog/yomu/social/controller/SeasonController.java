package id.ac.ui.cs.advprog.yomu.social.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import id.ac.ui.cs.advprog.yomu.social.dto.SeasonEndResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.SeasonStatusResponse;
import id.ac.ui.cs.advprog.yomu.social.service.season.SeasonService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/seasons")
@RequiredArgsConstructor
public class SeasonController {

    private final SeasonService seasonService;

    @GetMapping("/current")
    public ResponseEntity<SeasonStatusResponse> currentSeason() {
        return ResponseEntity.ok(seasonService.getCurrentSeason());
    }

    @PostMapping("/end")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeasonEndResponse> endSeason() {
        return ResponseEntity.ok(seasonService.endSeason());
    }
}
