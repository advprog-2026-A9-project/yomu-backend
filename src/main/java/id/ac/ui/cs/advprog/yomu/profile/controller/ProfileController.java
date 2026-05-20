package id.ac.ui.cs.advprog.yomu.profile.controller;

import id.ac.ui.cs.advprog.yomu.profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.yomu.profile.dto.UpdateBioRequest;
import id.ac.ui.cs.advprog.yomu.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(profileService.getProfileByUserIdOrUsername(authentication.getName()));
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable String identifier) {
        return ResponseEntity.ok(profileService.getProfileByUserIdOrUsername(identifier));
    }

    @PutMapping("/bio")
    public ResponseEntity<ProfileResponse> updateBio(Authentication authentication, @RequestBody UpdateBioRequest request) {
        ProfileResponse updated = profileService.updateBio(authentication.getName(), request.getBio());
        return ResponseEntity.ok(updated);
    }
}
