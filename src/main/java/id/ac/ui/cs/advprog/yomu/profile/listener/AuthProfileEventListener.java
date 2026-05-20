package id.ac.ui.cs.advprog.yomu.profile.listener;

import id.ac.ui.cs.advprog.yomu.auth.event.UserCreatedEvent;
import id.ac.ui.cs.advprog.yomu.auth.event.UserUpdatedEvent;
import id.ac.ui.cs.advprog.yomu.profile.model.Profile;
import id.ac.ui.cs.advprog.yomu.profile.repository.ProfileRepository;
import id.ac.ui.cs.advprog.yomu.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthProfileEventListener {

    private final ProfileRepository profileRepository;
    private final ProfileService profileService;

    @EventListener
    @Transactional
    public void onUserCreated(UserCreatedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Handling UserCreatedEvent for username: {}", event.getUsername());
        }
        profileService.getOrCreateProfile(event.getUsername(), event.getDisplayName());
    }

    @EventListener
    @Transactional
    public void onUserUpdated(UserUpdatedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Handling UserUpdatedEvent for username: {}", event.getUsername());
        }
        Profile profile = profileService.getOrCreateProfile(event.getUsername(), event.getDisplayName());
        profile.setDisplayName(event.getDisplayName());
        profileRepository.save(profile);
    }
}
