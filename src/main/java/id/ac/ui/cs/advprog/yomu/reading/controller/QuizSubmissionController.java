package id.ac.ui.cs.advprog.yomu.reading.controller;

import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizSubmissionRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizSubmissionResponse;
import id.ac.ui.cs.advprog.yomu.reading.service.QuizSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reading-texts/{readingTextId}/quiz")
@RequiredArgsConstructor
public class QuizSubmissionController {

    private final QuizSubmissionService quizSubmissionService;
    private final JwtUtil jwtUtil;

    private String getUserIdFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            final String token = authHeader.substring(7);
            return jwtUtil.extractUserId(token);
        }
        throw new RuntimeException("Token tidak valid");
    }

    @PostMapping("/submit")
    public ResponseEntity<QuizSubmissionResponse> submitQuiz(
            @PathVariable Long readingTextId,
            @RequestBody QuizSubmissionRequest request,
            @RequestHeader("Authorization") String authHeader) {

        final String userId = getUserIdFromHeader(authHeader);
        final QuizSubmissionResponse response =
                quizSubmissionService.submitQuiz(readingTextId, userId, request);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/completion")
    public ResponseEntity<Boolean> hasCompletedQuiz(
            @PathVariable Long readingTextId,
            @RequestHeader("Authorization") String authHeader) {

        final String userId = getUserIdFromHeader(authHeader);
        final boolean completed = quizSubmissionService.hasCompletedQuiz(readingTextId, userId);

        return ResponseEntity.ok(completed);
    }
}