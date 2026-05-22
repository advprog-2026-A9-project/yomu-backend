package id.ac.ui.cs.advprog.yomu.gamification.exception;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GamificationExceptionHandlerTest {

    private static final String MESSAGE_SHOULD_MATCH = "Message should match";

    private GamificationExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GamificationExceptionHandler();
    }

    @Test
    void testGamificationException_ConstructorWithMessage() {
        GamificationException ex1 = new GamificationException("msg1");
        assertAll("Verify constructor 1",
                () -> assertEquals("msg1", ex1.getMessage(), MESSAGE_SHOULD_MATCH),
                () -> assertEquals("GAMIFICATION_ERROR", ex1.getErrorCode(), "Error code should default to GAMIFICATION_ERROR"));
    }

    @Test
    void testGamificationException_ConstructorWithMessageAndErrorCode() {
        GamificationException ex2 = new GamificationException("msg2", "ERR_CODE_2");
        assertAll("Verify constructor 2",
                () -> assertEquals("msg2", ex2.getMessage(), MESSAGE_SHOULD_MATCH),
                () -> assertEquals("ERR_CODE_2", ex2.getErrorCode(), "Error code should match ERR_CODE_2"));
    }

    @Test
    void testGamificationException_ConstructorWithMessageAndCause() {
        Throwable cause = new RuntimeException("cause message");
        GamificationException ex3 = new GamificationException("msg3", cause);
        assertAll("Verify constructor 3",
                () -> assertEquals("msg3", ex3.getMessage(), MESSAGE_SHOULD_MATCH),
                () -> assertEquals("GAMIFICATION_ERROR", ex3.getErrorCode(), "Error code should default to GAMIFICATION_ERROR"),
                () -> assertSame(cause, ex3.getCause(), "Cause should match"));
    }

    @Test
    void testGamificationException_ConstructorWithMessageErrorCodeAndCause() {
        Throwable cause = new RuntimeException("cause message");
        GamificationException ex4 = new GamificationException("msg4", "ERR_CODE_4", cause);
        assertAll("Verify constructor 4",
                () -> assertEquals("msg4", ex4.getMessage(), MESSAGE_SHOULD_MATCH),
                () -> assertEquals("ERR_CODE_4", ex4.getErrorCode(), "Error code should match ERR_CODE_4"),
                () -> assertSame(cause, ex4.getCause(), "Cause should match"));
    }

    @Test
    void handleGamificationException_NotFoundErrorCode_ShouldReturn404() {
        GamificationException ex = new GamificationException("Not found", "NOT_FOUND");
        ResponseEntity<?> response = exceptionHandler.handleGamificationException(ex);

        assertAll("Verify 404 response",
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Status code should be NOT_FOUND"),
                () -> assertNotNull(response.getBody(), "Response body should not be null"));
    }

    @Test
    void handleGamificationException_DuplicateNameErrorCode_ShouldReturn409() {
        GamificationException ex = new GamificationException("Duplicate", "DUPLICATE_NAME");
        ResponseEntity<?> response = exceptionHandler.handleGamificationException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode(), "Status code should be CONFLICT");
    }

    @Test
    void handleGamificationException_BadRequestErrorCodes_ShouldReturn400() {
        String[] badRequestCodes = {
            "INVALID_REQUEST", "INVALID_NAME", "INVALID_MILESTONE", "INVALID_USER_ID", "INVALID_MASTER_ID",
            "INVALID_MILESTONE_TYPE", "INVALID_MILESTONE_THRESHOLD", "INVALID_MISSION_TYPE", "INVALID_TARGET_COUNT",
            "INVALID_REWARD_DESCRIPTION", "NAME_TOO_LONG", "MILESTONE_TOO_LONG"
        };

        for (String code : badRequestCodes) {
            GamificationException ex = new GamificationException("Bad request", code);
            ResponseEntity<?> response = exceptionHandler.handleGamificationException(ex);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Failed status code for code: " + code);
        }
    }

    @Test
    void handleGamificationException_UnknownErrorCode_ShouldReturn500() {
        GamificationException ex = new GamificationException("Unknown error", "SOME_OTHER_ERROR");
        ResponseEntity<?> response = exceptionHandler.handleGamificationException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "Status code should be INTERNAL_SERVER_ERROR");
    }
}
