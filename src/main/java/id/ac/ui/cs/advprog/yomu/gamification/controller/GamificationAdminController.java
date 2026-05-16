package id.ac.ui.cs.advprog.yomu.gamification.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementRequest;
import id.ac.ui.cs.advprog.yomu.gamification.dto.AchievementResponse;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionRequest;
import id.ac.ui.cs.advprog.yomu.gamification.dto.DailyMissionResponse;
import id.ac.ui.cs.advprog.yomu.gamification.service.AchievementService;
import id.ac.ui.cs.advprog.yomu.gamification.service.DailyMissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/gamification/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class GamificationAdminController {

    private final AchievementService achievementService;
    private final DailyMissionService dailyMissionService;

    @PostMapping("/achievements")
    @ResponseStatus(HttpStatus.CREATED)
    public AchievementResponse createAchievement(@Valid @RequestBody AchievementRequest request) {
        return achievementService.create(request);
    }

    @GetMapping("/achievements")
    public List<AchievementResponse> getAchievements() {
        return achievementService.findAll();
    }

    @PutMapping("/achievements/{achievementId}")
    public AchievementResponse updateAchievement(
        @PathVariable String achievementId,
        @Valid @RequestBody AchievementRequest request
    ) {
        return achievementService.update(achievementId, request);
    }

    @DeleteMapping("/achievements/{achievementId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAchievement(@PathVariable String achievementId) {
        achievementService.delete(achievementId);
    }

    @PostMapping("/daily-missions")
    @ResponseStatus(HttpStatus.CREATED)
    public DailyMissionResponse createDailyMission(@Valid @RequestBody DailyMissionRequest request) {
        return dailyMissionService.create(request);
    }

    @PutMapping("/daily-missions/{missionId}")
    public DailyMissionResponse updateDailyMission(
        @PathVariable String missionId,
        @Valid @RequestBody DailyMissionRequest request
    ) {
        return dailyMissionService.update(missionId, request);
    }

    @DeleteMapping("/daily-missions/{missionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDailyMission(@PathVariable String missionId) {
        dailyMissionService.delete(missionId);
    }

    @GetMapping("/daily-missions")
    public List<DailyMissionResponse> getDailyMissions() {
        return dailyMissionService.findAll();
    }

    @PostMapping("/daily-missions/select")
    @ResponseStatus(HttpStatus.OK)
    public void setTodayMissions(@RequestBody List<String> missionIds) {
        dailyMissionService.setTodayMissions(missionIds);
    }

    @PostMapping("/daily-missions/randomize")
    @ResponseStatus(HttpStatus.OK)
    public void randomizeTodayMissions() {
        dailyMissionService.forceRotateMissions();
    }
}
