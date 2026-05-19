package id.ac.ui.cs.advprog.yomu.profile.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.yomu.profile.model.Profile;
import id.ac.ui.cs.advprog.yomu.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfileByUserIdOrUsername(String identifier) {
        log.info("Querying read-model profile for identifier: {}", identifier);
        
        // Query strictly from our persistent read-only profile table
        Profile profile = profileRepository.findById(java.util.Objects.requireNonNull(identifier))
                .orElseThrow(() -> new IllegalArgumentException("Profil tidak ditemukan untuk user: " + identifier));

        // Map Reading Stats
        ProfileResponse.ReadingStatsDto readingStats = ProfileResponse.ReadingStatsDto.builder()
                .completedTexts(profile.getCompletedTexts())
                .totalMinutes(profile.getTotalMinutes())
                .quizAccuracy(profile.getQuizAccuracy())
                .build();

        // Deserialize Showcase Achievements
        List<ProfileResponse.ShowcaseAchievementDto> showcaseAchievements = new ArrayList<>();
        if (profile.getShowcaseAchievementsJson() != null && !profile.getShowcaseAchievementsJson().isEmpty()) {
            try {
                showcaseAchievements = objectMapper.readValue(
                        profile.getShowcaseAchievementsJson(),
                        new TypeReference<List<ProfileResponse.ShowcaseAchievementDto>>() {}
                );
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("Failed to deserialize showcase achievements JSON for user {}", profile.getUsername(), e);
                }
            }
        }

        return ProfileResponse.builder()
                .username(profile.getUsername())
                .displayName(profile.getDisplayName())
                .bio(profile.getBio())
                .joinedDate(formatJoinedDate(profile.getJoinedAt()))
                .clanName(profile.getClanName())
                .clanTier(profile.getClanTier())
                .readingStats(readingStats)
                .showcaseAchievements(showcaseAchievements)
                .build();
    }

    private String formatJoinedDate(LocalDateTime joinedAt) {
        if (joinedAt == null) {
            return "Mei 2026";
        }
        String[] months = {
                "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        };
        int monthIndex = joinedAt.getMonthValue() - 1;
        int year = joinedAt.getYear();
        if (monthIndex >= 0 && monthIndex < 12) {
            return months[monthIndex] + " " + year;
        }
        return "Mei " + year;
    }

    @Override
    @Transactional
    public ProfileResponse updateBio(String username, String bio) {
        if (log.isInfoEnabled()) {
            log.info("Updating bio for user: {} to: {}", username, bio);
        }
        
        if (bio != null && bio.length() > 100) {
            throw new IllegalArgumentException("Bio tidak boleh lebih dari 100 karakter");
        }
        
        Profile profile = profileRepository.findById(java.util.Objects.requireNonNull(username))
                .orElseThrow(() -> new IllegalArgumentException("Profil tidak ditemukan untuk user: " + username));

        profile.setBio(bio);
        profileRepository.save(profile);

        return getProfileByUserIdOrUsername(username);
    }
}
