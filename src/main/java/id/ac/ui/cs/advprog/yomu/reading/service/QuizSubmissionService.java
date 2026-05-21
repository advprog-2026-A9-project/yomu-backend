package id.ac.ui.cs.advprog.yomu.reading.service;

import id.ac.ui.cs.advprog.yomu.reading.dto.QuizSubmissionRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizSubmissionResponse;

import java.util.Map;

public interface QuizSubmissionService {
    QuizSubmissionResponse submitQuiz(Long readingTextId, String userId, QuizSubmissionRequest request);
    boolean hasCompletedQuiz(Long readingTextId, String userId);
    Map<String, Object> getCompletionStatus(Long readingTextId, String userId);
}