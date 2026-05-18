package id.ac.ui.cs.advprog.yomu.gamification.service;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.gamification.dto.ShowcaseUpdateRequest;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementShowcase;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserAchievementShowcaseRepository;
import id.ac.ui.cs.advprog.yomu.gamification.event.UserShowcaseAchievementChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AchievementShowcaseServiceImpl implements AchievementShowcaseService {

    private final UserAchievementShowcaseRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<String> getShowcaseByUserId(String userId) {
        Objects.requireNonNull(userId, "User ID must not be null");
        return repository.findById(userId)
                .map(UserAchievementShowcase::getAchievementIds)
                .orElse(new ArrayList<>());
    }

    @Override
    @Transactional
    public void updateShowcase(ShowcaseUpdateRequest request) {
        String userId = Objects.requireNonNull(request.getUserId(), "User ID must not be null");
        UserAchievementShowcase showcase = repository.findById(userId)
                .orElse(UserAchievementShowcase.builder()
                        .userId(userId)
                        .build());

        showcase.setAchievementIds(request.getAchievementIds());
        repository.save(showcase);

        eventPublisher.publishEvent(new UserShowcaseAchievementChangedEvent(userId, request.getAchievementIds()));
    }
}
