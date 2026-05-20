package id.ac.ui.cs.advprog.yomu.profile.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.yomu.gamification.event.UserShowcaseAchievementChangedEvent;
import id.ac.ui.cs.advprog.yomu.profile.dto.ProfileResponse;
import id.ac.ui.cs.advprog.yomu.profile.model.Profile;
import id.ac.ui.cs.advprog.yomu.profile.repository.ProfileRepository;
import id.ac.ui.cs.advprog.yomu.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GamificationProfileEventListener {

    private final ProfileRepository profileRepository;
    private final ProfileService profileService;
    private final ObjectMapper objectMapper;

    @EventListener
    @Transactional
    public void onUserShowcaseAchievementChanged(UserShowcaseAchievementChangedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("Handling UserShowcaseAchievementChangedEvent for user: {}", event.username());
        }
        Profile profile = profileService.getOrCreateProfile(event.username());
        
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
        
        profileRepository.save(profile);
    }
}
