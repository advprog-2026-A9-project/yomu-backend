package id.ac.ui.cs.advprog.yomu.reading.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "reading_texts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ReadingText {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private String category; // Misal: "Fiction", "News"
}