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

    /**
     * Clean Code: Mengambil User ID (Subject) langsung dari konteks autentikasi.
     * Mengurangi coupling dengan utility eksternal.
     */
    private String getUserIdFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // Dalam konfigurasi Spring Security berbasis JWT, "name" biasanya diisi dengan User ID (subject)
            return authentication.getName();
        }
        throw new RuntimeException("Pengguna tidak terautentikasi atau token tidak valid");
    }

    @PostMapping("/submit")
    public ResponseEntity<QuizSubmissionResponse> submitQuiz(
            @PathVariable Long readingTextId,
            @RequestBody QuizSubmissionRequest request) { // Parameter header Authorization dihapus

        final String userId = getUserIdFromSecurityContext();
        final QuizSubmissionResponse response = quizSubmissionService.submitQuiz(readingTextId, userId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/completion")
    public ResponseEntity<Boolean> hasCompletedQuiz(
            @PathVariable Long readingTextId) { // Parameter header Authorization dihapus

        final String userId = getUserIdFromSecurityContext();
        final boolean completed = quizSubmissionService.hasCompletedQuiz(readingTextId, userId);
        return ResponseEntity.ok(completed);
    }
}