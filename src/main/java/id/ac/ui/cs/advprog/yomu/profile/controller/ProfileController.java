package id.ac.ui.cs.advprog.yomu.profile.controller;

import id.ac.ui.cs.advprog.yomu.profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.yomu.profile.dto.UpdateBioRequest;
import id.ac.ui.cs.advprog.yomu.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(profileService.getProfileByUserIdOrUsername(principal.getName()));
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable String identifier) {
        return ResponseEntity.ok(profileService.getProfileByUserIdOrUsername(identifier));
    }

    @PutMapping("/bio")
    public ResponseEntity<ProfileResponse> updateBio(Principal principal, @RequestBody UpdateBioRequest request) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        ProfileResponse currentProfile = profileService.getProfileByUserIdOrUsername(principal.getName());
        ProfileResponse updated = profileService.updateBio(currentProfile.getUserId(), request.getBio());
        return ResponseEntity.ok(updated);
    }
}
