package id.ac.ui.cs.advprog.yomu.reading.controller;

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

    @PostMapping
    public ResponseEntity<ReadingTextResponse> createText(
            @RequestBody ReadingTextRequest request) {

        ReadingTextResponse response = readingTextService.createText(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ReadingTextResponse>> getAllTexts() {
        List<ReadingTextResponse> responses = readingTextService.getAllTexts();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteText(@PathVariable Long id) {

        readingTextService.deleteText(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReadingTextResponse> getTextById(@PathVariable Long id) {
        final ReadingTextResponse response = readingTextService.getTextById(id);
        return ResponseEntity.ok(response);
    }
}