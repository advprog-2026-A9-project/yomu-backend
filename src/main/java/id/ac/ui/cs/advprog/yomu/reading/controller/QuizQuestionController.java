package id.ac.ui.cs.advprog.yomu.reading.controller;

import id.ac.ui.cs.advprog.yomu.reading.dto.QuizQuestionRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.QuizQuestionResponse;
import id.ac.ui.cs.advprog.yomu.reading.service.QuizQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reading-texts/{readingTextId}/questions")
@RequiredArgsConstructor
public class QuizQuestionController {

    private final QuizQuestionService quizQuestionService;

    /**
     *Memusatkan logika ekstraksi Role dari Spring Security Context.
     * Controller tidak lagi perlu tahu cara membedah JWT.
     */
    private String getRoleFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            // Menghapus prefix "ROLE_" jika ada (standar Spring Security)
            return role.startsWith("ROLE_") ? role.substring(5) : role;
        }
        return "PELAJAR";
    }

    @PostMapping
    public ResponseEntity<QuizQuestionResponse> createQuestion(
            @PathVariable Long readingTextId,
            @RequestBody QuizQuestionRequest request) { // Parameter header Authorization dihapus

        final String role = getRoleFromSecurityContext();
        final QuizQuestionResponse response = quizQuestionService.createQuestion(readingTextId, request, role);
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
            @PathVariable Long questionId) { // Parameter header Authorization dihapus

        final String role = getRoleFromSecurityContext();
        quizQuestionService.deleteQuestion(questionId, role);
        return ResponseEntity.noContent().build();
    }
}