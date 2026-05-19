package id.ac.ui.cs.advprog.yomu.gamification.bootstrap;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.AccuracyBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedAchievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.AccuracyDailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.CountBasedDailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.repository.AchievementRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.DailyMissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class GamificationDataSeeder implements CommandLineRunner {

    private static final String ACHIEVE_ACCURACY = "achieve_accuracy";
    private static final String READ_N_ARTICLES = "read_n_articles";
    private static final String COMPLETE_N_QUIZZES = "complete_n_quizzes";
    private static final String GODLIKE_TIER = "GODLIKE";

    // Achievement Types (must match ProgressTrackingServiceImpl constants)
    private static final String ACHIEVEMENT_TYPE_READINGS_COMPLETED = "readings_completed";
    private static final String ACHIEVEMENT_TYPE_QUIZZES_PASSED = "quizzes_passed";
    private static final String ACHIEVEMENT_TYPE_ACCURACY_ABOVE = "accuracy_above";

    private final DailyMissionRepository dailyMissionRepository;
    private final AchievementRepository achievementRepository;

    @Override
    public void run(String... args) {
        log.info("Starting Gamification Data Seeding...");
        seedDailyMissions();
        seedAchievements();
        log.info("Gamification Data Seeding completed.");
    }

    private void seedDailyMissions() {
        List<DailyMission> missions = List.of(
                createMission("Reading Starter", "Read your first article of the day", READ_N_ARTICLES, 1, 50),
                createMission("Bookworm", "Read 3 articles to expand your knowledge", READ_N_ARTICLES, 3, 150),
                createMission("Speed Reader", "Complete 5 articles in one day", READ_N_ARTICLES, 5, 300),
                createMission("Quiz Novice", "Complete 1 quiz with any score", COMPLETE_N_QUIZZES, 1, 50),
                createMission("Quiz Master", "Complete 3 quizzes today", COMPLETE_N_QUIZZES, 3, 200),
                createMission("Academic Excellence", "Complete 5 quizzes in a single day", COMPLETE_N_QUIZZES, 5, 400),
                createMission("Sharpshooter", "Achieve 80% accuracy in a quiz", ACHIEVE_ACCURACY, 80, 100),
                createMission("Perfect Score", "Achieve 100% accuracy in any quiz", ACHIEVE_ACCURACY, 100, 250),
                createMission("Consistent Scholar", "Achieve at least 70% accuracy in 2 quizzes", ACHIEVE_ACCURACY, 70, 150),
                createMission("Elite Mind", "Achieve 95% accuracy in a challenging quiz", ACHIEVE_ACCURACY, 95, 200));

        int count = 0;
        for (DailyMission mission : missions) {
            if (!dailyMissionRepository.existsByName(mission.getName())) {
                dailyMissionRepository.save(mission);
                count++;
            }
        }
        if (count > 0)
            log.info("Seeded {} new daily missions.", count);
    }

    private void seedAchievements() {
        List<Achievement> achievements = List.of(
                // --- READING MILESTONES (Starter + 5 Tahap) ---
                createAchievement("First Steps", "Read 1 article", ACHIEVEMENT_TYPE_READINGS_COMPLETED, 1,
                        "BRONZE"),
                createAchievement("Literacy Enthusiast", "Read 50 articles", ACHIEVEMENT_TYPE_READINGS_COMPLETED, 50,
                        "SILVER"),
                createAchievement("Wisdom Gatherer", "Read 150 articles", ACHIEVEMENT_TYPE_READINGS_COMPLETED, 150,
                        "GOLD"),
                createAchievement("Library Resident", "Read 300 articles", ACHIEVEMENT_TYPE_READINGS_COMPLETED, 300,
                        "DIAMOND"),
                createAchievement("The Ultimate Reader", "Read 500 articles", ACHIEVEMENT_TYPE_READINGS_COMPLETED, 500,
                        "MYTHIC"),
                createAchievement("Knowledge Deity", "Read 1000 articles", ACHIEVEMENT_TYPE_READINGS_COMPLETED, 1000,
                        GODLIKE_TIER),

                // --- QUIZ MILESTONES (Starter + 5 Tahap) ---
                createAchievement("Quiz Initiate", "Pass 1 quiz", ACHIEVEMENT_TYPE_QUIZZES_PASSED, 1, "BRONZE"),
                createAchievement("Exam Crusher", "Pass 50 quizzes", ACHIEVEMENT_TYPE_QUIZZES_PASSED, 50, "SILVER"),
                createAchievement("Critical Thinker", "Pass 150 quizzes", ACHIEVEMENT_TYPE_QUIZZES_PASSED, 150, "GOLD"),
                createAchievement("Unstoppable Genius", "Pass 300 quizzes", ACHIEVEMENT_TYPE_QUIZZES_PASSED, 300,
                        "DIAMOND"),
                createAchievement("Sovereign of Scores", "Pass 500 quizzes", ACHIEVEMENT_TYPE_QUIZZES_PASSED, 500,
                        "MYTHIC"),
                createAchievement("Quiz Deity", "Pass 1000 quizzes", ACHIEVEMENT_TYPE_QUIZZES_PASSED, 1000, GODLIKE_TIER),

                // --- ACCURACY MILESTONES (NEW) ---
                createAccuracyAchievement("Perfect Score Rookie", "Get 100% accuracy 10 times", 100, 10, "SILVER"),
                createAccuracyAchievement("Perfect Score Master", "Get 100% accuracy 100 times", 100, 100, "DIAMOND"),
                createAccuracyAchievement("Perfect Score Legend", "Get 100% accuracy 1000 times", 100, 1000, GODLIKE_TIER),

                // --- CONTOH MANUAL OVERRIDE (Top 1 Clan in Season) ---
                // Threshold diset ke 0 karena ini bukan achievement berbasis progres numerik,
                // tapi tier diset langsung ke GODLIKE sehingga saat di-unlock akan dianggap
                // tertinggi.
                createAchievement("Season 1 Champion", "Top 1 Clan in Season 1 Leaderboard", "special_event", 0,
                        GODLIKE_TIER));

        int count = 0;
        for (Achievement ach : achievements) {
            if (!achievementRepository.existsByName(ach.getName())) {
                achievementRepository.save(ach);
                count++;
            }
        }
        if (count > 0)
            log.info("Seeded {} new achievements.", count);
    }

    private DailyMission createMission(String name, String milestone, String type, int target, int rewardScore) {
        DailyMission mission;
        if (ACHIEVE_ACCURACY.equals(type)) {
            AccuracyDailyMission accMission = new AccuracyDailyMission();
            accMission.setAccuracyThreshold(target);
            int reqCount = 1;
            if (name.contains("Consistent Scholar")) {
                reqCount = 2;
            }
            accMission.setRequiredCount(reqCount);
            mission = accMission;
        } else {
            CountBasedDailyMission countMission = new CountBasedDailyMission();
            countMission.setTargetCount(target);
            mission = countMission;
        }
        mission.setName(name);
        mission.setMilestone(milestone);
        mission.setMissionType(type);
        mission.setRewardScore(rewardScore);
        mission.setActive(true);
        // Make seeded missions active by default but let the system rotate and select them dynamically
        mission.setActiveFrom(null);
        mission.setActiveUntil(null);
        return mission;
    }

    private Achievement createAchievement(String name, String milestone, String type, int threshold, String tier) {
        CountBasedAchievement ach = new CountBasedAchievement();
        ach.setName(name);
        ach.setMilestone(milestone);
        ach.setMilestoneType(type);
        ach.setMilestoneThreshold(threshold);
        ach.setTier(tier);
        ach.setActive(true);
        return ach;
    }

    private Achievement createAccuracyAchievement(String name, String milestone, int accuracy, int count, String tier) {
        AccuracyBasedAchievement accAch = new AccuracyBasedAchievement();
        accAch.setName(name);
        accAch.setMilestone(milestone);
        accAch.setMilestoneType(ACHIEVEMENT_TYPE_ACCURACY_ABOVE);
        accAch.setAccuracyThreshold(accuracy);
        accAch.setMilestoneThreshold(count);
        accAch.setTier(tier);
        accAch.setActive(true);
        return accAch;
    }
}
