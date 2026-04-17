package id.ac.ui.cs.advprog.yomu.reading.dto;

public record QuizAnswerRequest(
        Long questionId,
        Long selectedOptionId
) {
}