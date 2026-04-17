package id.ac.ui.cs.advprog.yomu.reading.dto;

public record QuizOptionRequest(
        String optionText,
        boolean isCorrect
) {
}