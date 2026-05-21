package id.ac.ui.cs.advprog.yomu.social.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardResponse;
import id.ac.ui.cs.advprog.yomu.social.service.clan.query.ClanQueryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clans/leaderboard")
@RequiredArgsConstructor
public class ClanLeaderboardController {

    private final ClanQueryService queryService;

    @GetMapping
    public ResponseEntity<List<LeaderboardResponse>> getLeaderboard(
            final Authentication authentication,
            @RequestParam(value = "search", required = false) final String search) {
        final String username = (authentication != null) ? authentication.getName() : null;
        return ResponseEntity.ok(queryService.getLeaderboardByTier(username, search));
    }
}
