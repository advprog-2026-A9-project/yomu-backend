package id.ac.ui.cs.advprog.yomu.gamification.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.ProgressUpdateRequest;
import id.ac.ui.cs.advprog.yomu.gamification.service.achievement.AchievementProgressService;
import id.ac.ui.cs.advprog.yomu.gamification.service.mission.DailyMissionProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/gamification/progress")
@RequiredArgsConstructor
public class ProgressTrackingController {

    private final AchievementProgressService achievementProgressService;
    private final DailyMissionProgressService dailyMissionProgressService;

    @PostMapping("/achievements")
    @PreAuthorize("#request.username == authentication.name")
    public AchievementProgressResponse upsertAchievementProgress(@Valid @RequestBody ProgressUpdateRequest request) {
        return achievementProgressService.upsertAchievementProgress(request);
    }

    @PostMapping("/daily-missions")
    @PreAuthorize("#request.username == authentication.name")
    public DailyMissionProgressResponse upsertDailyMissionProgress(@Valid @RequestBody ProgressUpdateRequest request) {
        return dailyMissionProgressService.upsertDailyMissionProgress(request);
    }

    @GetMapping("/achievements")
    @PreAuthorize("#username == authentication.name")
    public List<AchievementProgressResponse> getAchievementProgress(@RequestParam String username) {
        return achievementProgressService.getAchievementProgressByUsername(username);
    }

    @GetMapping("/daily-missions")
    @PreAuthorize("#username == authentication.name")
    public List<DailyMissionProgressResponse> getTodayDailyMissionProgress(@RequestParam String username) {
        return dailyMissionProgressService.getTodayDailyMissionProgressByUsername(username);
    }

    @GetMapping("/daily-missions/today")
    @PreAuthorize("#username == authentication.name")
    public List<DailyMissionProgressResponse> getTodayDailyMissionDashboard(@RequestParam String username) {
        return dailyMissionProgressService.getTodayDailyMissionDashboard(username);
    }
}
