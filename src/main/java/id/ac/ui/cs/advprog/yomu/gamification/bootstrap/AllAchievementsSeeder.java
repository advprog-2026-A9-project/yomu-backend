package id.ac.ui.cs.advprog.yomu.gamification.bootstrap;

import id.ac.ui.cs.advprog.yomu.auth.model.User;
import id.ac.ui.cs.advprog.yomu.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.yomu.auth.event.UserCreatedEvent;
import id.ac.ui.cs.advprog.yomu.gamification.model.Achievement;
import id.ac.ui.cs.advprog.yomu.gamification.model.UserAchievementProgress;
import id.ac.ui.cs.advprog.yomu.gamification.repository.AchievementRepository;
import id.ac.ui.cs.advprog.yomu.gamification.repository.UserAchievementProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Order(2)
@Slf4j
public class AllAchievementsSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AchievementRepository achievementRepository;
    private final UserAchievementProgressRepository userAchievementProgressRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void run(String... args) {
        if (log.isInfoEnabled()) {
            log.info("Starting All Achievements User Seeding...");
        }
        
        String username = "champion";
        User existingUser = userRepository.findByUsername(username).orElse(null);
        if (existingUser != null) {
            if (log.isInfoEnabled()) {
                log.info("User 'champion' already exists. Unlocking any remaining achievements...");
            }
            unlockAllAchievements(existingUser.getUsername());
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail("champion@yomu.local");
        user.setDisplayName("Champion Player");
        user.setPassword(passwordEncoder.encode("Password123!"));
        user.setRole("USER");

        User savedUser = userRepository.save(user);
        if (log.isInfoEnabled()) {
            log.info("Seeded user 'champion' successfully with ID: {}", savedUser.getId());
        }

        // Publish UserCreatedEvent to trigger Profile creation in profile module
        eventPublisher.publishEvent(new UserCreatedEvent(this, savedUser.getId(), savedUser.getUsername(), savedUser.getDisplayName()));

        unlockAllAchievements(savedUser.getUsername());
        if (log.isInfoEnabled()) {
            log.info("All Achievements User Seeding completed successfully.");
        }
    }

    private void unlockAllAchievements(String username) {
        List<Achievement> achievements = achievementRepository.findAll();
        int unlockedCount = 0;

        for (Achievement achievement : achievements) {
            boolean alreadyExists = userAchievementProgressRepository
                    .findByUsernameAndAchievement(username, achievement)
                    .isPresent();

            if (!alreadyExists) {
                UserAchievementProgress progress = new UserAchievementProgress();
                progress.setUsername(username);
                progress.setAchievement(achievement);
                progress.setProgressValue(achievement.getMilestoneThreshold());
                progress.setUnlocked(true);
                progress.setUnlockedAt(LocalDateTime.now());

                userAchievementProgressRepository.save(progress);
                unlockedCount++;
            }
        }

        if (unlockedCount > 0 && log.isInfoEnabled()) {
            log.info("Unlocked {} achievements for user 'champion'.", unlockedCount);
        }
    }
}
