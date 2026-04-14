package id.ac.ui.cs.advprog.yomu.reading.controller;

import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import id.ac.ui.cs.advprog.yomu.reading.dto.ReadingTextRequest;
import id.ac.ui.cs.advprog.yomu.reading.dto.ReadingTextResponse;
import id.ac.ui.cs.advprog.yomu.reading.service.ReadingTextService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reading-texts")
@RequiredArgsConstructor
public class ReadingTextController {

    private final ReadingTextService readingTextService;
    private final JwtUtil jwtUtil; // Memanggil utility buatan temanmu


    private String getRoleFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractRole(token);
        }
        return "PELAJAR";
    }

    // 1. Admin dapat membuat teks bacaan
    @PostMapping
    public ResponseEntity<ReadingTextResponse> createText(
            @RequestBody ReadingTextRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String role = getRoleFromHeader(authHeader);
        ReadingTextResponse response = readingTextService.createText(request, role);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2. Pelajar (dan semua orang) dapat melihat daftar teks
    @GetMapping
    public ResponseEntity<List<ReadingTextResponse>> getAllTexts() {
        List<ReadingTextResponse> responses = readingTextService.getAllTexts();
        return ResponseEntity.ok(responses);
    }

    // 3. Admin dapat menghapus teks bacaan
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteText(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        String role = getRoleFromHeader(authHeader);
        readingTextService.deleteText(id, role);
        return ResponseEntity.noContent().build();
    }
}