package id.ac.ui.cs.advprog.yomu.reading.event;

public record QuizCompletedEvent(
        String userId,
        Long readingTextId,
        int score,
        int correctAnswers,
        int totalQuestions
) {}