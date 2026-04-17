package id.ac.ui.cs.advprog.yomu.reading.service;

import id.ac.ui.cs.advprog.yomu.reading.dto.QuizSubmissionRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizSubmissionResponse;
import id.ac.ui.cs.advprog.yomu.reading.repository.QuizQuestionRepository;
import id.ac.ui.cs.advprog.yomu.reading.repository.ReadingCompletionRepository;
import id.ac.ui.cs.advprog.yomu.reading.repository.ReadingTextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuizSubmissionServiceImpl implements QuizSubmissionService {

    private final ReadingTextRepository readingTextRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final ReadingCompletionRepository readingCompletionRepository;

    @Override
    public QuizSubmissionResponse submitQuiz(Long readingTextId, String userId, QuizSubmissionRequest request) {
        return null;
    }

    @Override
    public boolean hasCompletedQuiz(Long readingTextId, String userId) {
        return false;
    }
}