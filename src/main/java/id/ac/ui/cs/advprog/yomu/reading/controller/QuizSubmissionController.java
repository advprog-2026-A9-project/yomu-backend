package id.ac.ui.cs.advprog.yomu.reading.controller;

import id.ac.ui.cs.advprog.yomu.reading.dto.QuizSubmissionRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizSubmissionResponse;
import id.ac.ui.cs.advprog.yomu.reading.service.QuizSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reading-texts/{readingTextId}/quiz")
@RequiredArgsConstructor
public class QuizSubmissionController {

    private final QuizSubmissionService quizSubmissionService;

    private String getUserIdFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        throw new RuntimeException("Pengguna tidak terautentikasi atau token tidak valid");
    }

    @PostMapping("/submit")
    public ResponseEntity<QuizSubmissionResponse> submitQuiz(
            @PathVariable Long readingTextId,
            @RequestBody QuizSubmissionRequest request) {

        final String userId = getUserIdFromSecurityContext();
        final QuizSubmissionResponse response = quizSubmissionService.submitQuiz(readingTextId, userId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/completion")
    public ResponseEntity<Boolean> hasCompletedQuiz(
            @PathVariable Long readingTextId) {

        final String userId = getUserIdFromSecurityContext();
        final boolean completed = quizSubmissionService.hasCompletedQuiz(readingTextId, userId);
        return ResponseEntity.ok(completed);
    }
}