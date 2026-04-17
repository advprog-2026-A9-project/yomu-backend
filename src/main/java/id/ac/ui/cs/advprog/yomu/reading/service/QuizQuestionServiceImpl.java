package id.ac.ui.cs.advprog.yomu.reading.service;

import id.ac.ui.cs.advprog.yomu.reading.dto.QuizQuestionRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizQuestionResponse;
import id.ac.ui.cs.advprog.yomu.reading.repository.QuizOptionRepository;
import id.ac.ui.cs.advprog.yomu.reading.repository.QuizQuestionRepository;
import id.ac.ui.cs.advprog.yomu.reading.repository.ReadingTextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizQuestionServiceImpl implements QuizQuestionService {

    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final ReadingTextRepository readingTextRepository;

    @Override
    public QuizQuestionResponse createQuestion(Long readingTextId, QuizQuestionRequest request, String role) {
        return null;
    }

    @Override
    public List<QuizQuestionResponse> getQuestionsByReadingId(Long readingTextId) {
        return List.of();
    }

    @Override
    public void deleteQuestion(Long questionId, String role) {
        // stub
    }
}