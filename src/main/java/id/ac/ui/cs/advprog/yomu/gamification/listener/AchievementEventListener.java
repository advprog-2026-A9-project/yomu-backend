package id.ac.ui.cs.advprog.yomu.gamification.listener;

import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;
import id.ac.ui.cs.advprog.yomu.gamification.service.achievement.AchievementProgressService;
import id.ac.ui.cs.advprog.yomu.gamification.service.achievement.AchievementService;
import id.ac.ui.cs.advprog.yomu.gamification.strategy.AchievementProgressEvaluator;
import id.ac.ui.cs.advprog.yomu.gamification.strategy.QuizCompletionContext;
import id.ac.ui.cs.advprog.yomu.gamification.strategy.ReadingCompletionContext;
import id.ac.ui.cs.advprog.yomu.gamification.strategy.RankingContext;
import id.ac.ui.cs.advprog.yomu.reading.event.QuizCompletedEvent;
import id.ac.ui.cs.advprog.yomu.reading.event.ReadingCompletedEvent;
import id.ac.ui.cs.advprog.yomu.social.event.SeasonRankingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AchievementEventListener {

    private final AchievementService achievementService;
    private final AchievementProgressService achievementProgressService;
    private final List<AchievementProgressEvaluator> achievementEvaluators;

    @EventListener
    @Transactional
    public void onQuizCompleted(QuizCompletedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("AchievementEventListener received QuizCompletedEvent for userId={} score={}", event.userId(),
                    event.score());
        }
        processAchievementsForContext(event.userId(), new QuizCompletionContext(event.score()));
    }

    @EventListener
    @Transactional
    public void onReadingCompleted(ReadingCompletedEvent event) {
        if (log.isInfoEnabled()) {
            log.info("AchievementEventListener received ReadingCompletedEvent for username={}", event.getUsername());
        }
        processAchievementsForContext(event.getUsername(), new ReadingCompletionContext());
    }

    @EventListener
    @Transactional
    public void onSeasonRanking(SeasonRankingEvent event) {
        if (log.isInfoEnabled()) {
            log.info(
                    "AchievementEventListener received SeasonRankingEvent: clan '{}' finished rank #{} in {} tier ({} members)",
                    event.getClanName(),
                    event.getRank(),
                    event.getTier(),
                    event.getMemberUsernames().size());
        }

        for (String username : event.getMemberUsernames()) {
            processAchievementsForContext(username, new RankingContext(event.getRank(), event.getTier()));
        }
    }

    private void processAchievementsForContext(String username, Object context) {
        achievementService.getAllAchievements().stream()
                .filter(Achievement::isActive)
                .forEach(achievement -> {
                    UserAchievementProgress progress = achievementProgressService
                            .getOrCreateAchievementProgress(username, achievement);
                    if (evaluateAchievement(progress, context)) {
                        achievementProgressService.saveProgress(progress);
                    }
                });
    }

    private boolean evaluateAchievement(UserAchievementProgress progress, Object context) {
        String milestoneType = progress.getAchievement().getMilestoneType();
        for (AchievementProgressEvaluator evaluator : achievementEvaluators) {
            if (evaluator.supports(milestoneType)) {
                return evaluator.evaluate(progress, context);
            }
        }
        return false;
    }
}
