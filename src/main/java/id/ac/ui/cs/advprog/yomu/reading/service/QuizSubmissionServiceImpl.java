package id.ac.ui.cs.advprog.yomu.reading.service;

import id.ac.ui.cs.advprog.yomu.reading.dto.QuizAnswerRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizSubmissionRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizSubmissionResponse;
import id.ac.ui.cs.advprog.yomu.reading.model.QuizOption;
import id.ac.ui.cs.advprog.yomu.reading.model.QuizQuestion;
import id.ac.ui.cs.advprog.yomu.reading.model.ReadingCompletion;
import id.ac.ui.cs.advprog.yomu.reading.model.ReadingText;
import id.ac.ui.cs.advprog.yomu.reading.repository.QuizQuestionRepository;
import id.ac.ui.cs.advprog.yomu.reading.repository.ReadingCompletionRepository;
import id.ac.ui.cs.advprog.yomu.reading.repository.ReadingTextRepository;
import id.ac.ui.cs.advprog.yomu.reading.event.QuizCompletedEvent;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizSubmissionServiceImpl implements QuizSubmissionService {

    private final ReadingTextRepository readingTextRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final ReadingCompletionRepository readingCompletionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public QuizSubmissionResponse submitQuiz(Long readingTextId, String userId, QuizSubmissionRequest request) {
        if (hasCompletedQuiz(readingTextId, userId)) {
            throw new RuntimeException("User yang sudah selesai tidak boleh submit lagi");
        }

        ReadingText text = readingTextRepository.findById(readingTextId)
                .orElseThrow(() -> new RuntimeException("Harus exception jika reading text tidak ada"));

        List<QuizQuestion> questions = quizQuestionRepository.findByReadingTextId(readingTextId);
        int totalQuestions = questions.size();
        int correctAnswers = 0;

        Map<Long, Long> correctOptionMap = questions.stream()
                .collect(Collectors.toMap(
                        QuizQuestion::getId,
                        q -> q.getOptions().stream()
                                .filter(QuizOption::isCorrect)
                                .findFirst()
                                .map(QuizOption::getId)
                                .orElse(-1L)
                ));

        for (QuizAnswerRequest ans : request.answers()) {
            Long correctOptionId = correctOptionMap.get(ans.questionId());
            if (correctOptionId != null && correctOptionId.equals(ans.selectedOptionId())) {
                correctAnswers++;
            }
        }

        int score = totalQuestions > 0 ? (correctAnswers * 100) / totalQuestions : 0;

        ReadingCompletion completion = new ReadingCompletion();
        completion.setUserId(userId);
        completion.setReadingText(text);
        completion.setScore(score);
        completion.setCorrectAnswers(correctAnswers);
        completion.setTotalQuestions(totalQuestions);

        readingCompletionRepository.save(completion);

        eventPublisher.publishEvent(new QuizCompletedEvent(userId, readingTextId, score, correctAnswers, totalQuestions));

        return new QuizSubmissionResponse(totalQuestions, correctAnswers, score, true);
    }

    @Override
    public boolean hasCompletedQuiz(Long readingTextId, String userId) {
        return readingCompletionRepository.existsByUserIdAndReadingTextId(userId, readingTextId);
    }
}