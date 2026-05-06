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
import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
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

    @PostMapping
    public ResponseEntity<Clan> create(@RequestBody final ClanRequest request,
            @RequestHeader(SocialConstants.AUTHORIZATION_HEADER) String authHeader) {
        request.setUserId(getUserIdFromHeader(authHeader));
        request.setUsername(getUsernameFromHeader(authHeader));
        return ResponseEntity.ok(clanService.createClan(request));
    }

    @GetMapping
    public ResponseEntity<List<Clan>> getAll() {
        return ResponseEntity.ok(clanService.findAll());
    }

    @GetMapping("/me")
    public ResponseEntity<MyClanResponse> getMyClan(
            @RequestHeader(SocialConstants.AUTHORIZATION_HEADER) String authHeader) {
        String userId = getUserIdFromHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<MyClanResponse> myClan = clanService.getMyClanByUserId(userId);
        return myClan.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/edit")
    public ResponseEntity<Clan> edit(@PathVariable("id") String id,
            @RequestHeader(SocialConstants.AUTHORIZATION_HEADER) String authHeader,
            @RequestBody final ClanRequest request) {
        String userId = getUserIdFromHeader(authHeader);
        return ResponseEntity.ok(clanService.editClan(id, userId, request));
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<String> delete(@PathVariable("id") String id,
            @RequestHeader(SocialConstants.AUTHORIZATION_HEADER) String authHeader) {
        String userId = getUserIdFromHeader(authHeader);
        clanService.deleteClan(id, userId);
        return ResponseEntity.ok(SocialConstants.DELETE_SUCCESS_MESSAGE);
    }
}
