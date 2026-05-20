package id.ac.ui.cs.advprog.yomu.gamification.service.achievement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.gamification.dto.ShowcaseUpdateRequest;
import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementShowcase;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserAchievementShowcaseRepository;
import id.ac.ui.cs.advprog.yomu.gamification.event.UserShowcaseAchievementChangedEvent;
import id.ac.ui.cs.advprog.yomu.gamification.event.UserShowcaseAchievementChangedEvent.ShowcaseAchievementInfo;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AchievementShowcaseServiceImpl implements AchievementShowcaseService {

    private final UserAchievementShowcaseRepository repository;
    private final AchievementService achievementService;
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

        List<String> achievementIds = request.getAchievementIds() != null ? request.getAchievementIds() : new ArrayList<>();
        showcase.setAchievementIds(achievementIds);
        repository.save(showcase);

        List<Achievement> selected = achievementService.getAchievementsByIds(achievementIds);
        List<ShowcaseAchievementInfo> rich = selected.stream()
                .map(ach -> new ShowcaseAchievementInfo(
                        ach.getId(),
                        ach.getName(),
                        ach.getMilestone(),
                        ach.getTier()
                ))
                .toList();

        eventPublisher.publishEvent(new UserShowcaseAchievementChangedEvent(username, rich));
    }
}
