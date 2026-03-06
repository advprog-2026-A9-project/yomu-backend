package id.ac.ui.cs.advprog.yomu.social.controller;

import java.util.List;

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

    @PostMapping
    public ResponseEntity<Clan> create(@RequestBody final ClanRequest request,
                                       @RequestHeader("Authorization") String authHeader) {
        request.setUserId(getUserIdFromHeader(authHeader));
        return ResponseEntity.ok(clanService.createClan(request));
    }

    @GetMapping
    public ResponseEntity<List<Clan>> getAll() {
        return ResponseEntity.ok(clanService.findAll());
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<String> join(@PathVariable String id,
                                       @RequestHeader("Authorization") String authHeader) {
        String userId = getUserIdFromHeader(authHeader);
        clanService.joinClan(id, userId);
        return ResponseEntity.ok("Berhasil bergabung");
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
}