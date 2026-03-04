package id.ac.ui.cs.advprog.yomu.discussion.controller;

import id.ac.ui.cs.advprog.yomu.discussion.dto.CommentResponse;
import id.ac.ui.cs.advprog.yomu.discussion.dto.CreateCommentRequest;
import id.ac.ui.cs.advprog.yomu.discussion.service.DiscussionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/discussion")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DiscussionController {

    private final DiscussionService discussionService;

    @PostMapping("/create")
    public ResponseEntity<CommentResponse> createComment(@RequestBody CreateCommentRequest request) {
        return ResponseEntity.ok(discussionService.createComment(request));
    }

    @GetMapping("/reading/{readingId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByReading(@PathVariable UUID readingId) {
        return ResponseEntity.ok(discussionService.getCommentsByReading(readingId));
    }
}
