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
        clanService.joinClan(id, userId, username, SocialConstants.ROLE_MEMBER);
        return ResponseEntity.ok(SocialConstants.JOIN_SUCCESS_MESSAGE);
    }

    @PostMapping("/leave")
    public ResponseEntity<String> leave(@PathVariable("id") String id,
            @RequestHeader(SocialConstants.AUTHORIZATION_HEADER) String authHeader) {
        String userId = getUserIdFromHeader(authHeader);
        clanService.leaveClan(id, userId);
        return ResponseEntity.ok(SocialConstants.LEAVE_SUCCESS_MESSAGE);
    }
}
