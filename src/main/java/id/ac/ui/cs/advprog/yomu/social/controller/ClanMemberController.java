package id.ac.ui.cs.advprog.yomu.social.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.service.ClanService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clans/{id}")
@RequiredArgsConstructor
public class ClanMemberController {

    private final ClanService clanService;
    private final JwtUtil jwtUtil;

    private String getUserIdFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith(SocialConstants.BEARER_PREFIX)) {
            String token = authHeader.substring(SocialConstants.BEARER_PREFIX.length());
            return jwtUtil.extractUserId(token);
        }
        return null;
    }

    private String getUsernameFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith(SocialConstants.BEARER_PREFIX)) {
            String token = authHeader.substring(SocialConstants.BEARER_PREFIX.length());
            return jwtUtil.extractUsername(token);
        }
        return null;
    }

    @PostMapping("/join")
    public ResponseEntity<String> join(@PathVariable("id") String id,
            @RequestHeader(SocialConstants.AUTHORIZATION_HEADER) String authHeader) {
        String userId = getUserIdFromHeader(authHeader);
        String username = getUsernameFromHeader(authHeader);
        clanService.requestJoin(id, userId, username);
        return ResponseEntity.ok("Permintaan bergabung berhasil dikirim.");
    }

    @org.springframework.web.bind.annotation.GetMapping("/requests")
    public ResponseEntity<org.springframework.data.domain.Page<id.ac.ui.cs.advprog.yomu.social.dto.ClanJoinRequestResponse>> getRequests(
            @PathVariable("id") String id,
            @RequestHeader(SocialConstants.AUTHORIZATION_HEADER) String authHeader,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size) {
        String leaderId = getUserIdFromHeader(authHeader);
        return ResponseEntity.ok(clanService.getJoinRequests(id, leaderId, page, size));
    }

    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<String> acceptRequest(
            @PathVariable("id") String id,
            @PathVariable("requestId") Long requestId,
            @RequestHeader(SocialConstants.AUTHORIZATION_HEADER) String authHeader) {
        String leaderId = getUserIdFromHeader(authHeader);
        clanService.acceptJoinRequest(id, requestId, leaderId);
        return ResponseEntity.ok("Request diterima.");
    }

    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<String> rejectRequest(
            @PathVariable("id") String id,
            @PathVariable("requestId") Long requestId,
            @RequestHeader(SocialConstants.AUTHORIZATION_HEADER) String authHeader) {
        String leaderId = getUserIdFromHeader(authHeader);
        clanService.rejectJoinRequest(id, requestId, leaderId);
        return ResponseEntity.ok("Request ditolak.");
    }

    @PostMapping("/requests/reject-all")
    public ResponseEntity<String> rejectAllRequests(
            @PathVariable("id") String id,
            @RequestHeader(SocialConstants.AUTHORIZATION_HEADER) String authHeader) {
        String leaderId = getUserIdFromHeader(authHeader);
        clanService.rejectAllJoinRequests(id, leaderId);
        return ResponseEntity.ok("Semua request berhasil ditolak.");
    }

    @PostMapping("/requests/seed/{count}")
    public ResponseEntity<String> seedRequests(
            @PathVariable("id") String id,
            @PathVariable("count") int count) {
        clanService.seedJoinRequests(id, count);
        return ResponseEntity.ok("Berhasil menambahkan " + count + " request ke clan " + id);
    }

    @PostMapping("/requests/seed-members")
    public ResponseEntity<String> seedFullMembers(
            @PathVariable("id") String id) {
        clanService.seedFullMembers(id);
        return ResponseEntity.ok("Berhasil mengisi clan " + id + " hingga penuh (50 anggota).");
    }

    @PostMapping("/leave")
    public ResponseEntity<String> leave(@PathVariable("id") String id,
            @RequestHeader(SocialConstants.AUTHORIZATION_HEADER) String authHeader) {
        String userId = getUserIdFromHeader(authHeader);
        clanService.leaveClan(id, userId);
        return ResponseEntity.ok(SocialConstants.LEAVE_SUCCESS_MESSAGE);
    }
}
