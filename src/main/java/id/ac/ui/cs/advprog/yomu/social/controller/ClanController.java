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
import id.ac.ui.cs.advprog.yomu.common.util.InputSanitizer;
import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
import id.ac.ui.cs.advprog.yomu.social.dto.MyClanResponse;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.service.ClanService;
import id.ac.ui.cs.advprog.yomu.social.validation.ClanValidation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clans")
@RequiredArgsConstructor
public class ClanController {

    private final ClanService clanService;
    private final JwtUtil jwtUtil;
    private final InputSanitizer inputSanitizer;
    private final ClanValidation clanValidation;

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
        // Sanitize input to prevent XSS
        request.setName(inputSanitizer.sanitize(request.getName()));
        request.setDescription(inputSanitizer.sanitize(request.getDescription()));

        // Validate input length and content
        clanValidation.requireValidClanName(request.getName());
        clanValidation.requireValidClanDescription(request.getDescription());

        request.setUserId(getUserIdFromHeader(authHeader));
        request.setUsername(getUsernameFromHeader(authHeader));
        return ResponseEntity.ok(clanService.createClan(request));
    }

    @GetMapping
    public ResponseEntity<List<id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryResponse>> getAll() {
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
                .orElseGet(() -> ResponseEntity.ok().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<id.ac.ui.cs.advprog.yomu.social.dto.ClanDetailResponse> getClanDetail(
            @PathVariable("id") String id) {
        return ResponseEntity.ok(clanService.getClanDetail(id));
    }

    @PostMapping("/{id}/edit")
    public ResponseEntity<Clan> edit(@PathVariable("id") String id,
            @RequestHeader(SocialConstants.AUTHORIZATION_HEADER) String authHeader,
            @RequestBody final ClanRequest request) {
        // Sanitize input to prevent XSS
        request.setName(inputSanitizer.sanitize(request.getName()));
        request.setDescription(inputSanitizer.sanitize(request.getDescription()));

        // Validate input length and content
        clanValidation.requireValidClanName(request.getName());
        clanValidation.requireValidClanDescription(request.getDescription());

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

    @PostMapping("/{id}/kick/{memberId}")
    public ResponseEntity<String> kick(@PathVariable("id") String id,
            @PathVariable("memberId") String memberId,
            @RequestHeader(SocialConstants.AUTHORIZATION_HEADER) String authHeader) {
        String userId = getUserIdFromHeader(authHeader);
        clanService.kickMember(id, userId, memberId);
        return ResponseEntity.ok(SocialConstants.KICK_SUCCESS_MESSAGE);
    }
}
