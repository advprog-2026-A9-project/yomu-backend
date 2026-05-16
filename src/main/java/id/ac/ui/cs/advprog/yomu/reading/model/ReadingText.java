package id.ac.ui.cs.advprog.yomu.reading.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reading_texts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReadingText {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Builder.Default
    @OneToMany(mappedBy = "readingText", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuizQuestion> questions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "readingText", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReadingCompletion> completions = new ArrayList<>();

    // Custom Constructor untuk menyesuaikan dengan Unit Test (4 Parameter)
    public ReadingText(Long id, String title, String content, Category category) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;

    }
}