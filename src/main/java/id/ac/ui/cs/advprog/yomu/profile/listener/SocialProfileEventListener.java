package id.ac.ui.cs.advprog.yomu.profile.listener;

import id.ac.ui.cs.advprog.yomu.profile.model.Profile;
import id.ac.ui.cs.advprog.yomu.profile.repository.ProfileRepository;
import id.ac.ui.cs.advprog.yomu.profile.service.ProfileService;
import id.ac.ui.cs.advprog.yomu.social.event.ClanNameChangedEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserDeleteClanEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserJoinClanEvent;
import id.ac.ui.cs.advprog.yomu.social.event.UserLeaveClanEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocialProfileEventListener {

    private final ProfileRepository profileRepository;
    private final ProfileService profileService;

    @EventListener
    @Transactional
    public void onUserJoinClan(UserJoinClanEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Handling UserJoinClanEvent for user: {} and clan name: {}", event.getUsername(), event.getClanName());
        }
        Profile profile = profileService.getOrCreateProfile(event.getUsername());
        profile.setClanId(event.getClanId());
        profile.setClanName(event.getClanName());
        profile.setClanTier(event.getClanTier());
        profileRepository.save(profile);
    }

    @EventListener
    @Transactional
    public void onUserLeaveClan(UserLeaveClanEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Handling UserLeaveClanEvent for user: {}", event.getUsername());
        }
        Profile profile = profileService.getOrCreateProfile(event.getUsername());
        profile.setClanId(null);
        profile.setClanName(null);
        profile.setClanTier(null);
        profileRepository.save(profile);
    }

    @EventListener
    @Transactional
    public void onUserDeleteClan(UserDeleteClanEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Handling UserDeleteClanEvent for clan id: {}", event.getClanId());
        }
        List<Profile> profiles = profileRepository.findByClanId(event.getClanId());
        profiles.forEach(p -> {
            p.setClanId(null);
            p.setClanName(null);
            p.setClanTier(null);
        });
        profileRepository.saveAll(profiles);
    }

    @EventListener
    @Transactional
    public void onClanNameChanged(ClanNameChangedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Handling ClanNameChangedEvent for clan id: {} to: {}", event.getClanId(), event.getNewClanName());
        }
        List<Profile> profiles = profileRepository.findByClanId(event.getClanId());
        profiles.forEach(p -> p.setClanName(event.getNewClanName()));
        profileRepository.saveAll(profiles);
    }
}
