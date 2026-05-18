package id.ac.ui.cs.advprog.yomu.profile.service;

import id.ac.ui.cs.advprog.yomu.profile.dto.ProfileResponse;

public interface ProfileService {
    ProfileResponse getProfileByUserIdOrUsername(String identifier);
    ProfileResponse updateBio(String userId, String bio);
}
