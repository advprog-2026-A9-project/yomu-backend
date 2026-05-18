package id.ac.ui.cs.advprog.yomu.gamification.service;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.gamification.dto.ShowcaseUpdateRequest;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementShowcase;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserAchievementShowcaseRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.AchievementRepository;
import id.ac.ui.cs.advprog.yomu.gamification.event.UserShowcaseAchievementChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AchievementShowcaseServiceImpl implements AchievementShowcaseService {

    private static final String TYPE_READINGS = "readings_completed";
    private static final String TYPE_QUIZZES = "quizzes_passed";
    private static final String TYPE_ACCURACY = "accuracy_above";

    private static final String TIER_DIAMOND = "DIAMOND";
    private static final String TIER_GOLD = "GOLD";
    private static final String TIER_SILVER = "SILVER";
    private static final String TIER_BRONZE = "BRONZE";

    private static final int THRESHOLD_100 = 100;
    private static final int THRESHOLD_98 = 98;
    private static final int THRESHOLD_90 = 90;
    private static final int THRESHOLD_80 = 80;
    private static final int THRESHOLD_40 = 40;
    private static final int THRESHOLD_35 = 35;
    private static final int THRESHOLD_15 = 15;
    private static final int THRESHOLD_10 = 10;

    private static final String KEY_CENTURION = "Centurion";
    private static final String KEY_DIVINE = "Divine";
    private static final String KEY_ASCENDED = "Ascended";
    private static final String KEY_ETERNAL = "Eternal";

    private static final String COLOR_DIAMOND = "text-indigo-400 animate-pulse";
    private static final String COLOR_GOLD = "text-amber-400";
    private static final String COLOR_SILVER = "text-slate-300";
    private static final String COLOR_BRONZE = "text-amber-700";

    private final UserAchievementShowcaseRepository repository;
    private final AchievementRepository achievementRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<String> getShowcaseByUserId(String userId) {
        Objects.requireNonNull(userId, "User ID must not be null");
        return repository.findById(java.util.Objects.requireNonNull(userId))
                .map(UserAchievementShowcase::getAchievementIds)
                .orElse(new ArrayList<>());
    }

    @Override
    @Transactional
    public void updateShowcase(ShowcaseUpdateRequest request) {
        String userId = Objects.requireNonNull(request.getUserId(), "User ID must not be null");
        UserAchievementShowcase showcase = repository.findById(java.util.Objects.requireNonNull(userId))
                .orElse(UserAchievementShowcase.builder()
                        .userId(userId)
                        .build());

        showcase.setAchievementIds(request.getAchievementIds());
        repository.save(showcase);

        List<UserShowcaseAchievementChangedEvent.ShowcaseAchievementInfo> richAchievements = new ArrayList<>();
        if (request.getAchievementIds() != null) {
            for (String achId : request.getAchievementIds()) {
                achievementRepository.findById(java.util.Objects.requireNonNull(achId)).ifPresent(ach -> {
                    String tier = determineAchievementTier(ach);
                    String iconColor = determineAchievementIconColor(tier);
                    richAchievements.add(new UserShowcaseAchievementChangedEvent.ShowcaseAchievementInfo(
                            ach.getId(),
                            ach.getName(),
                            ach.getMilestone(),
                            tier,
                            iconColor
                    ));
                });
            }
        }

        eventPublisher.publishEvent(new UserShowcaseAchievementChangedEvent(userId, richAchievements));
    }

    private String determineAchievementTier(id.ac.ui.cs.advprog.yomu.gamification.model.Achievement ach) {
        String type = ach.getMilestoneType();
        int threshold = ach.getMilestoneThreshold();

        if (TYPE_READINGS.equalsIgnoreCase(type)) {
            if (threshold >= THRESHOLD_100) return TIER_DIAMOND;
            if (threshold >= THRESHOLD_35) return TIER_GOLD;
            if (threshold >= THRESHOLD_10) return TIER_SILVER;
            return TIER_BRONZE;
        } else if (TYPE_QUIZZES.equalsIgnoreCase(type)) {
            if (threshold >= THRESHOLD_100) return TIER_DIAMOND;
            if (threshold >= THRESHOLD_40) return TIER_GOLD;
            if (threshold >= THRESHOLD_15) return TIER_SILVER;
            return TIER_BRONZE;
        } else if (TYPE_ACCURACY.equalsIgnoreCase(type)) {
            String name = ach.getName();
            if (threshold >= THRESHOLD_98 || name.contains(KEY_CENTURION) || name.contains(KEY_DIVINE) || name.contains(KEY_ASCENDED) || name.contains(KEY_ETERNAL)) {
                return TIER_DIAMOND;
            }
            if (threshold >= THRESHOLD_90) return TIER_GOLD;
            if (threshold >= THRESHOLD_80) return TIER_SILVER;
            return TIER_BRONZE;
        }

        return TIER_BRONZE;
    }

    private String determineAchievementIconColor(String tier) {
        if (TIER_DIAMOND.equalsIgnoreCase(tier)) {
            return COLOR_DIAMOND;
        } else if (TIER_GOLD.equalsIgnoreCase(tier)) {
            return COLOR_GOLD;
        } else if (TIER_SILVER.equalsIgnoreCase(tier)) {
            return COLOR_SILVER;
        }
        return COLOR_BRONZE;
    }
}
