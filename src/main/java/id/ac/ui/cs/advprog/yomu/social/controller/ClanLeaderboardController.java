package id.ac.ui.cs.advprog.yomu.social.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardResponse;
import id.ac.ui.cs.advprog.yomu.social.service.ClanService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestHeader;
import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;

@RestController
@RequestMapping("/api/clans/leaderboard")
@RequiredArgsConstructor
public class ClanLeaderboardController {

    private final ClanService clanService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<List<LeaderboardResponse>> getLeaderboard(
            @RequestHeader(value = SocialConstants.AUTHORIZATION_HEADER, required = false) String authHeader) {
        String userId = getUserIdFromHeader(authHeader);
        return ResponseEntity.ok(clanService.getLeaderboardByTier(userId));
    }

    private String getUserIdFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}
