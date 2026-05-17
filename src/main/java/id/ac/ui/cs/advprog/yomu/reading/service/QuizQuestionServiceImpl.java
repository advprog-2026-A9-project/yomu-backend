package id.ac.ui.cs.advprog.yomu.reading.service;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizQuestionServiceImpl implements QuizQuestionService {

    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final ReadingTextRepository readingTextRepository;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public QuizQuestionResponse createQuestion(Long readingTextId, QuizQuestionRequest request) {
        ReadingText text = readingTextRepository.findById(readingTextId)
                .orElseThrow(() -> new RuntimeException("Harus melempar exception jika text bacaan induk tidak ditemukan"));

        QuizQuestion question = new QuizQuestion();
        question.setQuestionText(request.questionText());
        question.setReadingText(text);

        QuizQuestion savedQuestion = quizQuestionRepository.save(question);

        List<QuizOptionResponse> optionResponses = request.options().stream().map(optReq -> {
            QuizOption option = new QuizOption();
            option.setOptionText(optReq.optionText());
            option.setCorrect(optReq.isCorrect());
            option.setQuizQuestion(savedQuestion);
            QuizOption savedOpt = quizOptionRepository.save(option);
            return new QuizOptionResponse(savedOpt.getId(), savedOpt.getOptionText());
        }).collect(Collectors.toList());

        return new QuizQuestionResponse(savedQuestion.getId(), savedQuestion.getQuestionText(), optionResponses);
    }

    @Override
    public List<QuizQuestionResponse> getQuestionsByReadingId(Long readingTextId) {
        return quizQuestionRepository.findByReadingTextId(readingTextId).stream().map(q -> {
            List<QuizOptionResponse> opts = q.getOptions().stream()
                    .map(o -> new QuizOptionResponse(o.getId(), o.getOptionText()))
                    .collect(Collectors.toList());
            return new QuizQuestionResponse(q.getId(), q.getQuestionText(), opts);
        }).collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteQuestion(Long questionId) {
        if (!quizQuestionRepository.existsById(questionId)) {
            throw new RuntimeException("Harus melempar exception jika pertanyaan yang ingin dihapus tidak ada");
        }
        quizQuestionRepository.deleteById(questionId);
    }
}