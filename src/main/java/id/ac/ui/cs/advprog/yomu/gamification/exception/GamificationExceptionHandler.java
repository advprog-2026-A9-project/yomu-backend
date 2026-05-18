package id.ac.ui.cs.advprog.yomu.gamification.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for gamification module
 * Dependency Inversion: high-level code doesn't need to know about HTTP response handling
 */
@RestControllerAdvice
public class GamificationExceptionHandler {

    private record ErrorResponse(String errorCode, String message) {}

    @ExceptionHandler(GamificationException.class)
    public ResponseEntity<ErrorResponse> handleGamificationException(GamificationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getErrorCode(),
            ex.getMessage()
        );

        // Determine HTTP status based on error code
        HttpStatus status = mapErrorCodeToHttpStatus(ex.getErrorCode());
        return new ResponseEntity<>(errorResponse, status);
    }

    @NonNull
    private HttpStatus mapErrorCodeToHttpStatus(String errorCode) {
        return switch (errorCode) {
            case "NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "DUPLICATE_NAME" -> HttpStatus.CONFLICT;
            case "INVALID_REQUEST", "INVALID_NAME", "INVALID_MILESTONE", "INVALID_USER_ID", "INVALID_MASTER_ID" ->
                HttpStatus.BAD_REQUEST;
            case "INVALID_MILESTONE_TYPE", "INVALID_MILESTONE_THRESHOLD", "INVALID_MISSION_TYPE", "INVALID_TARGET_COUNT",
                 "INVALID_REWARD_DESCRIPTION", "NAME_TOO_LONG", "MILESTONE_TOO_LONG" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
