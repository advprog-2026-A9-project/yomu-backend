package id.ac.ui.cs.advprog.yomu.reading.dto;

public record QuizSubmissionResponse(
        int totalQuestions,
        int correctAnswers,
        int score,
        boolean completed
) {
}