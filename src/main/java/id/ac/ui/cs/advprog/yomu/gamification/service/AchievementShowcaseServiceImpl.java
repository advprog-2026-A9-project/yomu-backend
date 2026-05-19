package id.ac.ui.cs.advprog.yomu.gamification.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.gamification.dto.ShowcaseUpdateRequest;
import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementShowcase;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserAchievementShowcaseRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.AchievementRepository;
import id.ac.ui.cs.advprog.yomu.gamification.event.UserShowcaseAchievementChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AchievementShowcaseServiceImpl implements AchievementShowcaseService {

    private final UserAchievementShowcaseRepository repository;
    private final AchievementRepository achievementRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<String> getShowcaseByUsername(String username) {
        return repository.findById(Objects.requireNonNull(username, "Username must not be null"))
                .map(UserAchievementShowcase::getAchievementIds)
                .orElse(new ArrayList<>());
    }

    @Override
    @Transactional
    public void updateShowcase(ShowcaseUpdateRequest request) {
        String username = Objects.requireNonNull(request.getUsername(), "Username must not be null");
        UserAchievementShowcase showcase = repository.findById(username)
                .orElse(UserAchievementShowcase.builder()
                        .username(username)
                        .build());

        showcase.setAchievementIds(request.getAchievementIds());
        repository.save(showcase);

        List<UserShowcaseAchievementChangedEvent.ShowcaseAchievementInfo> richAchievements = new ArrayList<>();
        if (request.getAchievementIds() != null && !request.getAchievementIds().isEmpty()) {
            @SuppressWarnings("null")
            Iterable<Achievement> achievements = achievementRepository.findAllById(Objects.requireNonNull(request.getAchievementIds()));
            for (Achievement ach : achievements) {
                richAchievements.add(new UserShowcaseAchievementChangedEvent.ShowcaseAchievementInfo(
                        ach.getId(),
                        ach.getName(),
                        ach.getMilestone(),
                        ach.getTier()
                ));
            }
        }

        eventPublisher.publishEvent(new UserShowcaseAchievementChangedEvent(username, richAchievements));
    }
}
