package id.ac.ui.cs.advprog.yomu.reading.dto;

import java.util.List;

public record QuizQuestionResponse(
        Long id,
        String questionText,
        List<QuizOptionResponse> options
) {
}