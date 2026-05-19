package id.ac.ui.cs.advprog.yomu.profile.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "display_name")
    private String displayName;

    @Column(length = 100)
    private String bio;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    // Clan Info
    @Column(name = "clan_id")
    private String clanId;

    @Column(name = "clan_name")
    private String clanName;

    @Column(name = "clan_tier")
    private String clanTier;

    // Reading Stats
    @Column(name = "completed_texts")
    private int completedTexts;

    @Column(name = "total_minutes")
    private int totalMinutes;

    @Column(name = "quiz_accuracy")
    private int quizAccuracy;

    @Column(name = "correct_answers_sum")
    private int correctAnswersSum;

    @Column(name = "total_questions_sum")
    private int totalQuestionsSum;

    // Showcase Achievements serialized as JSON
    @Lob
    @Column(name = "showcase_achievements_json", columnDefinition = "TEXT")
    private String showcaseAchievementsJson;
}
