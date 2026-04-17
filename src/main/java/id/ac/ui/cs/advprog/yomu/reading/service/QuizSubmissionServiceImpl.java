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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizSubmissionServiceImpl implements QuizSubmissionService {

    private final ReadingTextRepository readingTextRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final ReadingCompletionRepository readingCompletionRepository;

    @Override
    public QuizSubmissionResponse submitQuiz(Long readingTextId, String userId, QuizSubmissionRequest request) {
        if (readingCompletionRepository.existsByUserIdAndReadingTextId(userId, readingTextId)) {
            throw new RuntimeException("Kuis untuk bacaan ini sudah diselesaikan");
        }

        final ReadingText readingText = readingTextRepository.findById(readingTextId)
                .orElseThrow(() -> new RuntimeException("Teks bacaan tidak ditemukan"));

        final List<QuizQuestion> questions = quizQuestionRepository.findByReadingTextId(readingTextId);

        if (questions.isEmpty()) {
            throw new RuntimeException("Belum ada pertanyaan kuis untuk bacaan ini");
        }

        final Map<Long, Long> answersByQuestionId = request.answers().stream()
                .collect(Collectors.toMap(
                        QuizAnswerRequest::questionId,
                        QuizAnswerRequest::selectedOptionId
                ));

        int correctAnswers = 0;
        final int totalQuestions = questions.size();

        for (final QuizQuestion question : questions) {
            final Long selectedOptionId = answersByQuestionId.get(question.getId());
            if (selectedOptionId != null && isCorrectAnswer(question, selectedOptionId)) {
                correctAnswers++;
            }
        }

        final int score = (correctAnswers * 100) / totalQuestions;

        final ReadingCompletion completion = new ReadingCompletion();
        completion.setUserId(userId);
        completion.setReadingText(readingText);
        completion.setScore(score);
        completion.setCorrectAnswers(correctAnswers);
        completion.setTotalQuestions(totalQuestions);

        readingCompletionRepository.save(completion);

        return new QuizSubmissionResponse(
                totalQuestions,
                correctAnswers,
                score,
                true
        );
    }

    @Override
    public boolean hasCompletedQuiz(Long readingTextId, String userId) {
        return readingCompletionRepository.existsByUserIdAndReadingTextId(userId, readingTextId);
    }

    private boolean isCorrectAnswer(QuizQuestion question, Long selectedOptionId) {
        final Map<Long, QuizOption> optionById = question.getOptions().stream()
                .collect(Collectors.toMap(QuizOption::getId, Function.identity()));

        final QuizOption selectedOption = optionById.get(selectedOptionId);
        return selectedOption != null && selectedOption.isCorrect();
    }
}