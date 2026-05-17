package id.ac.ui.cs.advprog.yomu.gamification.bootstrap;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.DailyMission;
import id.ac.ui.cs.advprog.yomu.gamification.repository.AchievementRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.DailyMissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class GamificationDataSeeder implements CommandLineRunner {

    private static final String ACHIEVE_ACCURACY = "achieve_accuracy";
    private static final String READ_N_ARTICLES = "read_n_articles";
    private static final String COMPLETE_N_QUIZZES = "complete_n_quizzes";

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
                createMission("Reading Starter", "Read your first article of the day", READ_N_ARTICLES, 1, "50 Score"),
                createMission("Bookworm", "Read 3 articles to expand your knowledge", READ_N_ARTICLES, 3, "150 Score"),
                createMission("Speed Reader", "Complete 5 articles in one day", READ_N_ARTICLES, 5, "300 Score"),
                createMission("Quiz Novice", "Complete 1 quiz with any score", COMPLETE_N_QUIZZES, 1, "50 Score"),
                createMission("Quiz Master", "Complete 3 quizzes today", COMPLETE_N_QUIZZES, 3, "200 Score"),
                createMission("Academic Excellence", "Complete 5 quizzes in a single day", COMPLETE_N_QUIZZES, 5,
                        "400 Score"),
                createMission("Sharpshooter", "Achieve 80% accuracy in a quiz", ACHIEVE_ACCURACY, 80, "100 Score"),
                createMission("Perfect Score", "Achieve 100% accuracy in any quiz", ACHIEVE_ACCURACY, 100, "250 Score"),
                createMission("Consistent Scholar", "Achieve at least 70% accuracy in 2 quizzes", ACHIEVE_ACCURACY, 70,
                        "150 Score"),
                createMission("Elite Mind", "Achieve 95% accuracy in a challenging quiz", ACHIEVE_ACCURACY, 95,
                        "200 Score"));

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
                // --- READING MILESTONES (15) ---
                createAchievement("First Page", "Read your first 5 articles", ACHIEVEMENT_TYPE_READINGS_COMPLETED, 5),
                createAchievement("Quick Learner", "Read 10 articles", ACHIEVEMENT_TYPE_READINGS_COMPLETED, 10),
                createAchievement("Consistent Reader", "Read 20 articles", ACHIEVEMENT_TYPE_READINGS_COMPLETED, 20),
                createAchievement("Knowledge Seeker", "Read 35 articles", ACHIEVEMENT_TYPE_READINGS_COMPLETED, 35),
                createAchievement("Literacy Enthusiast", "Read 50 articles", ACHIEVEMENT_TYPE_READINGS_COMPLETED, 50),
                createAchievement("Bibliophile", "Read 75 articles", ACHIEVEMENT_TYPE_READINGS_COMPLETED, 75),
                createAchievement("Information Junkie", "Read 100 articles", ACHIEVEMENT_TYPE_READINGS_COMPLETED, 100),
                createAchievement("Deep Diver", "Read 125 articles", ACHIEVEMENT_TYPE_READINGS_COMPLETED, 125),
                createAchievement("Wisdom Gatherer", "Read 150 articles", ACHIEVEMENT_TYPE_READINGS_COMPLETED, 150),
                createAchievement("Archivist", "Read 175 articles", ACHIEVEMENT_TYPE_READINGS_COMPLETED, 175),
                createAchievement("Polymath in Training", "Read 200 articles", ACHIEVEMENT_TYPE_READINGS_COMPLETED, 200),
                createAchievement("Walking Encyclopedia", "Read 250 articles", ACHIEVEMENT_TYPE_READINGS_COMPLETED, 250),
                createAchievement("Library Resident", "Read 300 articles", ACHIEVEMENT_TYPE_READINGS_COMPLETED, 300),
                createAchievement("Knowledge Overlord", "Read 400 articles", ACHIEVEMENT_TYPE_READINGS_COMPLETED, 400),
                createAchievement("The Ultimate Reader", "Read 500 articles", ACHIEVEMENT_TYPE_READINGS_COMPLETED, 500),

                // --- QUIZ MILESTONES (15) ---
                createAchievement("Quiz Starter", "Pass 5 quizzes", ACHIEVEMENT_TYPE_QUIZZES_PASSED, 5),
                createAchievement("Academic Apprentice", "Pass 10 quizzes", ACHIEVEMENT_TYPE_QUIZZES_PASSED, 10),
                createAchievement("Test Taker", "Pass 15 quizzes", ACHIEVEMENT_TYPE_QUIZZES_PASSED, 15),
                createAchievement("Study Buddy", "Pass 25 quizzes", ACHIEVEMENT_TYPE_QUIZZES_PASSED, 25),
                createAchievement("Exam Crusher", "Pass 40 quizzes", ACHIEVEMENT_TYPE_QUIZZES_PASSED, 40),
                createAchievement("Knowledge Validator", "Pass 60 quizzes", ACHIEVEMENT_TYPE_QUIZZES_PASSED, 60),
                createAchievement("Quiz Veteran", "Pass 80 quizzes", ACHIEVEMENT_TYPE_QUIZZES_PASSED, 80),
                createAchievement("Master of Inquiry", "Pass 100 quizzes", ACHIEVEMENT_TYPE_QUIZZES_PASSED, 100),
                createAchievement("The Analyzer", "Pass 125 quizzes", ACHIEVEMENT_TYPE_QUIZZES_PASSED, 125),
                createAchievement("Critical Thinker", "Pass 150 quizzes", ACHIEVEMENT_TYPE_QUIZZES_PASSED, 150),
                createAchievement("Intellectual Beast", "Pass 200 quizzes", ACHIEVEMENT_TYPE_QUIZZES_PASSED, 200),
                createAchievement("Quiz God", "Pass 250 quizzes", ACHIEVEMENT_TYPE_QUIZZES_PASSED, 250),
                createAchievement("Unstoppable Genius", "Pass 300 quizzes", ACHIEVEMENT_TYPE_QUIZZES_PASSED, 300),
                createAchievement("The Oracle", "Pass 400 quizzes", ACHIEVEMENT_TYPE_QUIZZES_PASSED, 400),
                createAchievement("Sovereign of Scores", "Pass 500 quizzes", ACHIEVEMENT_TYPE_QUIZZES_PASSED, 500),

                // --- ACCURACY EXCELLENCE (20) ---
                createAchievement("First Strike", "Get 100% in 1 quiz", ACHIEVEMENT_TYPE_ACCURACY_ABOVE, 100),
                createAchievement("Sharpened Mind", "Get 90% or more in 5 quizzes", ACHIEVEMENT_TYPE_ACCURACY_ABOVE, 90),
                createAchievement("High Achiever", "Get 85% or more in 10 quizzes", ACHIEVEMENT_TYPE_ACCURACY_ABOVE, 85),
                createAchievement("Sniper Precision", "Get 100% in 5 quizzes", ACHIEVEMENT_TYPE_ACCURACY_ABOVE, 100),
                createAchievement("Excellent Merit", "Get 95% or more in 15 quizzes", ACHIEVEMENT_TYPE_ACCURACY_ABOVE, 95),
                createAchievement("Perfect streak", "Get 100% in 10 quizzes", ACHIEVEMENT_TYPE_ACCURACY_ABOVE, 100),
                createAchievement("Elite Scholar", "Get 90% or more in 25 quizzes", ACHIEVEMENT_TYPE_ACCURACY_ABOVE, 90),
                createAchievement("Silver Tongue", "Get 80% or more in 40 quizzes", ACHIEVEMENT_TYPE_ACCURACY_ABOVE, 80),
                createAchievement("Gold Standard", "Get 100% in 20 quizzes", ACHIEVEMENT_TYPE_ACCURACY_ABOVE, 100),
                createAchievement("Diamond Accuracy", "Get 95% or more in 35 quizzes", ACHIEVEMENT_TYPE_ACCURACY_ABOVE, 95),
                createAchievement("Flawless Logic", "Get 100% in 30 quizzes", ACHIEVEMENT_TYPE_ACCURACY_ABOVE, 100),
                createAchievement("Master of Truth", "Get 90% or more in 50 quizzes", ACHIEVEMENT_TYPE_ACCURACY_ABOVE, 90),
                createAchievement("Divine Insight", "Get 100% in 50 quizzes", ACHIEVEMENT_TYPE_ACCURACY_ABOVE, 100),
                createAchievement("The Perfectionist", "Get 100% in 75 quizzes", ACHIEVEMENT_TYPE_ACCURACY_ABOVE, 100),
                createAchievement("Centurion", "Get 100% in 100 quizzes", ACHIEVEMENT_TYPE_ACCURACY_ABOVE, 100),
                createAchievement("Godly Focus", "Get 98% or more in 125 quizzes", ACHIEVEMENT_TYPE_ACCURACY_ABOVE, 98),
                createAchievement("Impeccable Record", "Get 100% in 150 quizzes", ACHIEVEMENT_TYPE_ACCURACY_ABOVE, 100),
                createAchievement("Legend of Literacy", "Get 95% or more in 200 quizzes", ACHIEVEMENT_TYPE_ACCURACY_ABOVE, 95),
                createAchievement("Ascended Mind", "Get 100% in 250 quizzes", ACHIEVEMENT_TYPE_ACCURACY_ABOVE, 100),
                createAchievement("Eternal Wisdom", "Get 100% in 500 quizzes", ACHIEVEMENT_TYPE_ACCURACY_ABOVE, 100)
        );

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

    private DailyMission createMission(String name, String milestone, String type, int target, String reward) {
        DailyMission mission = new DailyMission();
        mission.setName(name);
        mission.setMilestone(milestone);
        mission.setMissionType(type);
        mission.setTargetCount(target);
        mission.setRewardDescription(reward);
        mission.setActive(true);
        return mission;
    }

    private Achievement createAchievement(String name, String milestone, String type, int threshold) {
        Achievement ach = new Achievement();
        ach.setName(name);
        ach.setMilestone(milestone);
        ach.setMilestoneType(type);
        ach.setMilestoneThreshold(threshold);
        ach.setActive(true);
        return ach;
    }
}
