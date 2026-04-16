package id.ac.ui.cs.advprog.yomu.social.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
import id.ac.ui.cs.advprog.yomu.social.dto.LeaderboardResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.MyClanResponse;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.service.ClanService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clans")
@RequiredArgsConstructor
public class ClanController {

    private final ClanService clanService;
    private final JwtUtil jwtUtil;

    private String getUserIdFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractUserId(token);
        }
        return null;
    }

    private String getUsernameFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractUsername(token);
        }
        return null;
    }

    @PostMapping
    public ResponseEntity<Clan> create(@RequestBody final ClanRequest request,
            @RequestHeader("Authorization") String authHeader) {
        request.setUserId(getUserIdFromHeader(authHeader));
        request.setUsername(getUsernameFromHeader(authHeader));
        return ResponseEntity.ok(clanService.createClan(request));
    }

    @GetMapping
    public ResponseEntity<List<Clan>> getAll() {
        return ResponseEntity.ok(clanService.findAll());
    }

    @GetMapping("/me")
    public ResponseEntity<MyClanResponse> getMyClan(@RequestHeader("Authorization") String authHeader) {
        String userId = getUserIdFromHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<MyClanResponse> myClan = clanService.getMyClanByUserId(userId);
        return myClan.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<String> join(@PathVariable String id,
            @RequestHeader("Authorization") String authHeader) {
        String userId = getUserIdFromHeader(authHeader);
        String username = getUsernameFromHeader(authHeader);
        clanService.joinClan(id, userId, username, "MEMBER");
        return ResponseEntity.ok("Berhasil bergabung");
    }

    @PostMapping("/{id}/edit")
    public ResponseEntity<String> edit(@PathVariable String id,
        @RequestHeader("Authorization") String authHeader, @RequestBody final ClanRequest request) {
            String userId = getUserIdFromHeader(authHeader);
            clanService.editClan(id, userId, request);
            return ResponseEntity.ok("Berhasil mengubah informasi clan");
        }

    @PostMapping("/{id}/leave")
    public ResponseEntity<String> leave(@PathVariable String id,
            @RequestHeader("Authorization") String authHeader) {
        String userId = getUserIdFromHeader(authHeader);
        clanService.leaveClan(id, userId);
        return ResponseEntity.ok("Berhasil keluar dari clan");
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<String> delete(@PathVariable String id,
            @RequestHeader("Authorization") String authHeader) {
        String userId = getUserIdFromHeader(authHeader);
        clanService.deleteClan(id, userId);
        return ResponseEntity.ok("Clan berhasil dihapus");
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardResponse>> getLeaderboard() {
        return ResponseEntity.ok(clanService.getLeaderboardByTier());
    }

    @PostMapping("/admin/end-season")
    public ResponseEntity<String> endSeason(@RequestHeader("Authorization") String authHeader) {
        clanService.endSeason();
        return ResponseEntity.ok("Season ended. Clans promoted/demoted.");
    }
}