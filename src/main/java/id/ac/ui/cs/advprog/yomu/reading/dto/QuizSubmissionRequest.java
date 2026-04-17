package id.ac.ui.cs.advprog.yomu.reading.dto;

import java.util.List;

public record QuizSubmissionRequest(
        List<QuizAnswerRequest> answers
) {
}