package id.ac.ui.cs.advprog.yomu.reading.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reading_completions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReadingCompletion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId; // Berasal dari Modul Auth

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reading_text_id", nullable = false)
    private ReadingText readingText;

    @Column(nullable = false)
    private int score;

    @Column(name = "correct_answers", nullable = false)
    private int correctAnswers;

    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;
}