package id.ac.ui.cs.advprog.yomu.profile.service;

import id.ac.ui.cs.advprog.yomu.profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.yomu.profile.model.Profile;

public interface ProfileService {
    ProfileResponse getProfileByUserIdOrUsername(String identifier);
    ProfileResponse updateBio(String username, String bio);
    Profile getOrCreateProfile(String username);
    Profile getOrCreateProfile(String username, String defaultDisplayName);
}
