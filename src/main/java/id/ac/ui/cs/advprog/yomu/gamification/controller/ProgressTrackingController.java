package id.ac.ui.cs.advprog.yomu.gamification.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionProgressResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.ProgressUpdateRequest;
import id.ac.ui.cs.advprog.yomu.gamification.service.ProgressTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/gamification/progress")
@RequiredArgsConstructor
public class ProgressTrackingController {

    private final ProgressTrackingService progressTrackingService;

    @PostMapping("/achievements")
    public AchievementProgressResponse upsertAchievementProgress(@Valid @RequestBody ProgressUpdateRequest request) {
        return progressTrackingService.upsertAchievementProgress(request);
    }

    @PostMapping("/daily-missions")
    public DailyMissionProgressResponse upsertDailyMissionProgress(@Valid @RequestBody ProgressUpdateRequest request) {
        return progressTrackingService.upsertDailyMissionProgress(request);
    }

    @GetMapping("/achievements")
    public List<AchievementProgressResponse> getAchievementProgress(@RequestParam String username) {
        return progressTrackingService.getAchievementProgressByUsername(username);
    }

    @GetMapping("/daily-missions")
    public List<DailyMissionProgressResponse> getTodayDailyMissionProgress(@RequestParam String username) {
        return progressTrackingService.getTodayDailyMissionProgressByUsername(username);
    }

    @GetMapping("/daily-missions/today")
    public List<DailyMissionProgressResponse> getTodayDailyMissionDashboard(@RequestParam String username) {
        return progressTrackingService.getTodayDailyMissionDashboard(username);
    }
}
