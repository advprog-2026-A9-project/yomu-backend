package id.ac.ui.cs.advprog.yomu.gamification.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import id.ac.ui.cs.advprog.yomu.gamification.dto.ShowcaseUpdateRequest;
import id.ac.ui.cs.advprog.yomu.gamification.service.achievement.AchievementShowcaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/gamification/showcase")
@RequiredArgsConstructor
public class AchievementShowcaseController {

    private final AchievementShowcaseService showcaseService;

    @GetMapping
    public List<String> getShowcase(@RequestParam String username) {
        return showcaseService.getShowcaseByUsername(username);
    }

    @PutMapping
    @PreAuthorize("#request.username == authentication.name")
    public void updateShowcase(@Valid @RequestBody ShowcaseUpdateRequest request) {
        showcaseService.updateShowcase(request);
    }
}
