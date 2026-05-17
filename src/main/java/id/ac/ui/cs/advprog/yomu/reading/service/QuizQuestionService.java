package id.ac.ui.cs.advprog.yomu.reading.service;

import id.ac.ui.cs.advprog.yomu.reading.dto.QuizQuestionRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizQuestionResponse;

import java.util.List;

public interface QuizQuestionService {
    QuizQuestionResponse createQuestion(Long readingTextId, QuizQuestionRequest request);
    List<QuizQuestionResponse> getQuestionsByReadingId(Long readingTextId);
    void deleteQuestion(Long questionId);
}