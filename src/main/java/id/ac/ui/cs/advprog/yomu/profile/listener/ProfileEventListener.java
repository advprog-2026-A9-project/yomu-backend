package id.ac.ui.cs.advprog.yomu.profile.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.auth.event.UserCreatedEvent;
import id.ac.ui.cs.advprog.yomu.auth.event.UserUpdatedEvent;
import id.ac.ui.cs.advprog.yomu.gamification.event.UserShowcaseAchievementChangedEvent;
import id.ac.ui.cs.advprog.yomu.profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.yomu.profile.model.Profile;
import id.ac.ui.cs.advprog.yomu.profile.repository.ProfileRepository;
import id.ac.ui.cs.advprog.yomu.reading.event.QuizCompletedEvent;
import id.ac.ui.cs.advprog.yomu.social.event.ClanNameChangedEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserDeleteClanEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserJoinClanEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserLeaveClanEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProfileEventListener {

    private final ProfileRepository profileRepository;
    private final ObjectMapper objectMapper;

    @EventListener
    @Transactional
    public void onUserCreated(UserCreatedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Handling UserCreatedEvent for user id: {}", event.getUserId());
        }
        getOrCreateProfile(event.getUserId(), event.getUsername(), event.getDisplayName());
    }

    @EventListener
    @Transactional
    public void onUserUpdated(UserUpdatedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Handling UserUpdatedEvent for user id: {}", event.getUserId());
        }
        Profile profile = getOrCreateProfile(event.getUserId(), event.getUsername(), event.getDisplayName());
        profile.setUsername(event.getUsername());
        profile.setDisplayName(event.getDisplayName());
        profileRepository.save(java.util.Objects.requireNonNull(profile));
    }

    @EventListener
    @Transactional
    public void onQuizCompleted(QuizCompletedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Handling QuizCompletedEvent for user id: {}", event.userId());
        }
        Profile profile = getOrCreateProfile(event.userId());
        
        profile.setCompletedTexts(profile.getCompletedTexts() + 1);
        profile.setTotalMinutes(profile.getCompletedTexts() * 8);
        
        profile.setCorrectAnswersSum(profile.getCorrectAnswersSum() + event.correctAnswers());
        profile.setTotalQuestionsSum(profile.getTotalQuestionsSum() + event.totalQuestions());
        
        if (profile.getTotalQuestionsSum() > 0) {
            profile.setQuizAccuracy((int) Math.round((profile.getCorrectAnswersSum() * 100.0) / profile.getTotalQuestionsSum()));
        }
        
        profileRepository.save(java.util.Objects.requireNonNull(profile));
    }

    @EventListener
    @Transactional
    public void onUserJoinClan(UserJoinClanEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Handling UserJoinClanEvent for user id: {} and clan name: {}", event.getUserId(), event.getClanName());
        }
        Profile profile = getOrCreateProfile(event.getUserId());
        profile.setClanId(event.getClanId());
        profile.setClanName(event.getClanName());
        profile.setClanTier(event.getClanTier());
        profileRepository.save(java.util.Objects.requireNonNull(profile));
    }

    @EventListener
    @Transactional
    public void onUserLeaveClan(UserLeaveClanEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Handling UserLeaveClanEvent for user id: {}", event.getUserId());
        }
        Profile profile = getOrCreateProfile(event.getUserId());
        profile.setClanId(null);
        profile.setClanName(null);
        profile.setClanTier(null);
        profileRepository.save(java.util.Objects.requireNonNull(profile));
    }

    @EventListener
    @Transactional
    public void onUserDeleteClan(UserDeleteClanEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Handling UserDeleteClanEvent for clan id: {}", event.getClanId());
        }
        List<Profile> profiles = profileRepository.findByClanId(event.getClanId());
        for (Profile profile : profiles) {
            profile.setClanId(null);
            profile.setClanName(null);
            profile.setClanTier(null);
            profileRepository.save(java.util.Objects.requireNonNull(profile));
        }
    }

    @EventListener
    @Transactional
    public void onClanNameChanged(ClanNameChangedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Handling ClanNameChangedEvent for clan id: {} to: {}", event.getClanId(), event.getNewClanName());
        }
        List<Profile> profiles = profileRepository.findByClanId(event.getClanId());
        for (Profile profile : profiles) {
            profile.setClanName(event.getNewClanName());
            profileRepository.save(java.util.Objects.requireNonNull(profile));
        }
    }

    @EventListener
    @Transactional
    public void onUserShowcaseAchievementChanged(UserShowcaseAchievementChangedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Handling UserShowcaseAchievementChangedEvent for user id: {}", event.userId());
        }
        Profile profile = getOrCreateProfile(event.userId());
        
        List<ProfileResponse.ShowcaseAchievementDto> dtoList = new ArrayList<>();
        if (event.achievements() != null) {
            for (var achInfo : event.achievements()) {
                dtoList.add(ProfileResponse.ShowcaseAchievementDto.builder()
                        .id(achInfo.id())
                        .name(achInfo.name())
                        .description(achInfo.description())
                        .tier(achInfo.tier())
                        .build());
            }
        }

        try {
            String json = objectMapper.writeValueAsString(dtoList);
            profile.setShowcaseAchievementsJson(json);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to serialize showcase achievements to JSON", e);
            }
            profile.setShowcaseAchievementsJson("[]");
        }
        
        profileRepository.save(java.util.Objects.requireNonNull(profile));
    }

    private Profile getOrCreateProfile(String userId) {
        return getOrCreateProfile(userId, "user_" + userId, "User " + userId);
    }

    private Profile getOrCreateProfile(String userId, String defaultUsername, String defaultDisplayName) {
        return profileRepository.findById(java.util.Objects.requireNonNull(userId))
                .orElseGet(() -> {
                    Profile profile = Profile.builder()
                            .userId(userId)
                            .username(defaultUsername)
                            .displayName(defaultDisplayName)
                            .bio("📖 Yomu avid reader | Seeking knowledge every single day.")
                            .joinedAt(LocalDateTime.now())
                            .completedTexts(0)
                            .totalMinutes(0)
                            .quizAccuracy(0)
                            .correctAnswersSum(0)
                            .totalQuestionsSum(0)
                            .showcaseAchievementsJson("[]")
                            .build();
                    return profileRepository.save(java.util.Objects.requireNonNull(profile));
                });
    }
}
