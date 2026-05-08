package id.ac.ui.cs.advprog.yomu.reading.controller;

import id.ac.ui.cs.advprog.yomu.reading.dto.ReadingTextRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.ReadingTextResponse;
import id.ac.ui.cs.advprog.yomu.reading.service.ReadingTextService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reading-texts")
@RequiredArgsConstructor
public class ReadingTextController {

    private final ReadingTextService readingTextService;

    // Fungsi baru untuk mengambil Role dari Spring Security
    private String getRoleFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            // Mengambil role pertama yang dimiliki user (misal: "ADMIN" atau "PELAJAR")
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            // Jika role di-prefix dengan "ROLE_" (standar Spring), kita hapus prefix-nya
            return role.startsWith("ROLE_") ? role.substring(5) : role;
        }
        return "PELAJAR";
    }

    @PostMapping
    public ResponseEntity<ReadingTextResponse> createText(
            @RequestBody ReadingTextRequest request) { // Parameter header DIHAPUS

        String role = getRoleFromSecurityContext();
        ReadingTextResponse response = readingTextService.createText(request, role);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ReadingTextResponse>> getAllTexts() {
        List<ReadingTextResponse> responses = readingTextService.getAllTexts();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteText(@PathVariable Long id) { // Parameter header DIHAPUS

        String role = getRoleFromSecurityContext();
        readingTextService.deleteText(id, role);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReadingTextResponse> getTextById(@PathVariable Long id) {
        final ReadingTextResponse response = readingTextService.getTextById(id);
        return ResponseEntity.ok(response);
    }
}