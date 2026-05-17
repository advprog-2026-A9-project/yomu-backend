package id.ac.ui.cs.advprog.yomu.reading.controller;

import id.ac.ui.cs.advprog.yomu.reading.dto.QuizQuestionRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizQuestionResponse;
import id.ac.ui.cs.advprog.yomu.reading.service.QuizQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reading-texts/{readingTextId}/questions")
@RequiredArgsConstructor
public class QuizQuestionController {

    private final QuizQuestionService quizQuestionService;

    @PostMapping
    public ResponseEntity<QuizQuestionResponse> createQuestion(
            @PathVariable Long readingTextId,
            @RequestBody QuizQuestionRequest request) {

        final QuizQuestionResponse response = quizQuestionService.createQuestion(readingTextId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<QuizQuestionResponse>> getQuestionsByReadingId(
            @PathVariable Long readingTextId) {

        final List<QuizQuestionResponse> responses = quizQuestionService.getQuestionsByReadingId(readingTextId);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long readingTextId,
            @PathVariable Long questionId) {

        quizQuestionService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }
}