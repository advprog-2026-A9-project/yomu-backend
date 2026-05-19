package id.ac.ui.cs.advprog.yomu.social.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "clan_quiz_stats")
@Getter
@Setter
public class ClanQuizStats {
    @Id
    @Column(nullable = false)
    private String clanId;

    @Column(nullable = false)
    private long totalQuizAttempts = 0;

    @Column(nullable = false)
    private long totalCorrectAnswers = 0;

    @Column(nullable = false)
    private long totalQuestions = 0;

    @Column(nullable = false)
    private long totalScore = 0;

    @Column(nullable = true, length = 1000)
    private String rollingQuizHistory = "";
}
