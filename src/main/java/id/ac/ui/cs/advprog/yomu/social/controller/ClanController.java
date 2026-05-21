package id.ac.ui.cs.advprog.yomu.social.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanDetailResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanRequest;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanSummaryResponse;
import id.ac.ui.cs.advprog.yomu.social.dto.MyClanResponse;
import id.ac.ui.cs.advprog.yomu.social.model.Clan;
import id.ac.ui.cs.advprog.yomu.social.service.clan.lifecycle.ClanLifecycleService;
import id.ac.ui.cs.advprog.yomu.social.service.clan.membership.ClanMembershipService;
import id.ac.ui.cs.advprog.yomu.social.service.clan.query.ClanQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clans")
@RequiredArgsConstructor
public class ClanController {

    private final ClanLifecycleService lifecycleService;
    private final ClanQueryService queryService;
    private final ClanMembershipService membershipService;

    @PostMapping
    public ResponseEntity<Clan> create(@RequestBody @Valid final ClanRequest request,
            final Authentication authentication) {
        final String username = authentication.getName();
        request.setUsername(username);
        return ResponseEntity.ok(lifecycleService.createClan(request));
    }

    @GetMapping
    public ResponseEntity<List<ClanSummaryResponse>> getAll(
            @RequestParam(value = "search", required = false) final String search,
            @RequestParam(value = "random", defaultValue = "false") final boolean random) {
        if (random && (search == null || search.isBlank())) {
            return ResponseEntity.ok(queryService.findRandomClans(10));
        }
        return ResponseEntity.ok(queryService.findAll(search));
    }

    @GetMapping("/me")
    public ResponseEntity<MyClanResponse> getMyClan(final Authentication authentication) {
        final String username = authentication.getName();
        final Optional<MyClanResponse> myClan = queryService.getMyClanByUsername(username);
        return myClan.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClanDetailResponse> getClanDetail(@PathVariable("id") final String id) {
        return ResponseEntity.ok(queryService.getClanDetail(id));
    }

    @PostMapping("/{id}/edit")
    public ResponseEntity<Clan> edit(
            @PathVariable("id") final String id,
            final Authentication authentication,
            @RequestBody @Valid final ClanRequest request) {
        final String username = authentication.getName();
        return ResponseEntity.ok(lifecycleService.editClan(id, username, request));
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<String> delete(
            @PathVariable("id") final String id,
            final Authentication authentication) {
        final String username = authentication.getName();
        lifecycleService.deleteClan(id, username);
        return ResponseEntity.ok(SocialConstants.DELETE_SUCCESS_MESSAGE);
    }

    @PostMapping("/{id}/kick/{memberId}")
    public ResponseEntity<String> kick(
            @PathVariable("id") final String id,
            @PathVariable("memberId") final String memberId,
            final Authentication authentication) {
        final String username = authentication.getName();
        membershipService.kickMember(id, username, memberId);
        return ResponseEntity.ok(SocialConstants.KICK_SUCCESS_MESSAGE);
    }
}
