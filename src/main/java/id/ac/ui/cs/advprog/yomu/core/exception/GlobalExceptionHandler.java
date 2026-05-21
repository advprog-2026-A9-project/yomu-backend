package id.ac.ui.cs.advprog.yomu.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException exception) {
        String message = exception.getMessage();
        HttpStatus status = HttpStatus.BAD_REQUEST; // Default 400

        if (message != null) {
            // 409 Conflict: Bentrok aturan bisnis (Penuh, Sudah tergabung, dll)
            if (message.contains("tergabung") || message.contains("penuh") || message.contains("anggota")) {
                status = HttpStatus.CONFLICT;
            } 
            // 403 Forbidden: Masalah Hak Akses (Bukan ketua, tidak ada izin)
            else if (message.contains("permission") || message.contains("ketua") || message.contains("Leader")) {
                status = HttpStatus.FORBIDDEN;
            }
        }

        return ResponseEntity.status(status)
            .body(Map.of("message", message != null ? message : "Invalid state request"));
    }
}