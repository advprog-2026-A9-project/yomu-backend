package id.ac.ui.cs.advprog.yomu.reading.dto;

import java.util.List;

public record QuizQuestionRequest(
        String questionText,
        List<QuizOptionRequest> options
) {
}