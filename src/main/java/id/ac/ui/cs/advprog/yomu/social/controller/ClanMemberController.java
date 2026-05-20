package id.ac.ui.cs.advprog.yomu.social.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import id.ac.ui.cs.advprog.yomu.social.constant.SocialConstants;
import id.ac.ui.cs.advprog.yomu.social.dto.ClanJoinRequestResponse;
import id.ac.ui.cs.advprog.yomu.social.service.clan.joinrequest.ClanJoinRequestService;
import id.ac.ui.cs.advprog.yomu.social.service.clan.membership.ClanMembershipService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clans/{id}")
@RequiredArgsConstructor
public class ClanMemberController {

    private final ClanJoinRequestService joinRequestService;
    private final ClanMembershipService membershipService;

    @PostMapping("/join")
    public ResponseEntity<String> join(@PathVariable("id") final String id, final Authentication authentication) {
        final String username = authentication.getName();
        joinRequestService.requestJoin(id, username);
        return ResponseEntity.ok("Permintaan bergabung berhasil dikirim.");
    }

    @GetMapping("/requests")
    public ResponseEntity<Page<ClanJoinRequestResponse>> getRequests(
            @PathVariable("id") final String id,
            final Authentication authentication,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size) {
        final String leaderUsername = authentication.getName();
        return ResponseEntity.ok(joinRequestService.getJoinRequests(id, leaderUsername, page, size));
    }

    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<String> acceptRequest(
            @PathVariable("id") final String id,
            @PathVariable("requestId") final Long requestId,
            final Authentication authentication) {
        final String leaderUsername = authentication.getName();
        joinRequestService.acceptJoinRequest(id, requestId, leaderUsername);
        return ResponseEntity.ok("Request diterima.");
    }

    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<String> rejectRequest(
            @PathVariable("id") final String id,
            @PathVariable("requestId") final Long requestId,
            final Authentication authentication) {
        final String leaderUsername = authentication.getName();
        joinRequestService.rejectJoinRequest(id, requestId, leaderUsername);
        return ResponseEntity.ok("Request ditolak.");
    }

    @PostMapping("/requests/reject-all")
    public ResponseEntity<String> rejectAllRequests(
            @PathVariable("id") final String id,
            final Authentication authentication) {
        final String leaderUsername = authentication.getName();
        joinRequestService.rejectAllJoinRequests(id, leaderUsername);
        return ResponseEntity.ok("Semua request berhasil ditolak.");
    }

    @PostMapping("/leave")
    public ResponseEntity<String> leave(@PathVariable("id") final String id, final Authentication authentication) {
        final String username = authentication.getName();
        membershipService.leaveClan(id, username);
        return ResponseEntity.ok(SocialConstants.LEAVE_SUCCESS_MESSAGE);
    }
}
