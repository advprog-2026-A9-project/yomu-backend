package id.ac.ui.cs.advprog.yomu.social.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardResponse;
import id.ac.ui.cs.advprog.yomu.social.service.ClanService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clans/leaderboard")
@RequiredArgsConstructor
public class ClanLeaderboardController {

    private final ClanService clanService;

    @GetMapping
    public ResponseEntity<List<LeaderboardResponse>> getLeaderboard() {
        return ResponseEntity.ok(clanService.getLeaderboardByTier());
    }
}
