package id.ac.ui.cs.advprog.yomu.reading.service;

import id.ac.ui.cs.advprog.yomu.reading.dto.QuizOptionRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizOptionResponse;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizQuestionRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizQuestionResponse;
import id.ac.ui.cs.advprog.yomu.reading.model.QuizOption;
import id.ac.ui.cs.advprog.yomu.reading.model.QuizQuestion;
import id.ac.ui.cs.advprog.yomu.reading.model.ReadingText;
import id.ac.ui.cs.advprog.yomu.reading.repository.QuizOptionRepository;
import id.ac.ui.cs.advprog.yomu.reading.repository.QuizQuestionRepository;
import id.ac.ui.cs.advprog.yomu.reading.repository.ReadingTextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizQuestionServiceImpl implements QuizQuestionService {

    private static final String ADMIN_ROLE = "ADMIN";

    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final ReadingTextRepository readingTextRepository;

    @Override
    public QuizQuestionResponse createQuestion(Long readingTextId, QuizQuestionRequest request, String role) {
        validateAdmin(role);

        final ReadingText readingText = readingTextRepository.findById(readingTextId)
                .orElseThrow(() -> new RuntimeException("Teks bacaan tidak ditemukan"));

        final QuizQuestion question = new QuizQuestion();
        question.setQuestionText(request.questionText());
        question.setReadingText(readingText);

        final QuizQuestion savedQuestion = quizQuestionRepository.save(question);

        final List<QuizOption> savedOptions = request.options().stream()
                .map(optionRequest -> saveOption(savedQuestion, optionRequest))
                .toList();

        savedQuestion.setOptions(savedOptions);

        return toResponse(savedQuestion);
    }

    @Override
    public List<QuizQuestionResponse> getQuestionsByReadingId(Long readingTextId) {
        return quizQuestionRepository.findByReadingTextId(readingTextId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void deleteQuestion(Long questionId, String role) {
        validateAdmin(role);

        if (!quizQuestionRepository.existsById(questionId)) {
            throw new RuntimeException("Pertanyaan kuis tidak ditemukan");
        }

        quizQuestionRepository.deleteById(questionId);
    }

    private void validateAdmin(String role) {
        if (!ADMIN_ROLE.equalsIgnoreCase(role)) {
            throw new RuntimeException("Hanya Admin yang dapat melakukan aksi ini.");
        }
    }

    private QuizOption saveOption(QuizQuestion question, QuizOptionRequest request) {
        final QuizOption option = new QuizOption();
        option.setOptionText(request.optionText());
        option.setCorrect(request.isCorrect());
        option.setQuizQuestion(question);

        final QuizOption savedOption = quizOptionRepository.save(option);
        return savedOption != null ? savedOption : option;
    }

    private QuizQuestionResponse toResponse(QuizQuestion question) {
        final List<QuizOptionResponse> options = question.getOptions().stream()
                .map(option -> new QuizOptionResponse(
                        option.getId(),
                        option.getOptionText()
                ))
                .toList();

        return new QuizQuestionResponse(
                question.getId(),
                question.getQuestionText(),
                options
        );
    }
}