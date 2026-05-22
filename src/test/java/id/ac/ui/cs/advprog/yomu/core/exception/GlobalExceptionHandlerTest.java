package id.ac.ui.cs.advprog.yomu.core.exception;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleIllegalArgumentException_ShouldReturnBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument test");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleIllegalArgumentException(ex);

        assertAll("Verify response",
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status should be BAD_REQUEST"),
                () -> assertNotNull(response.getBody(), "Response body should not be null"),
                () -> assertEquals("Invalid argument test", response.getBody().get("message"), "Message should match exception message"));
    }

    @Test
    void handleIllegalStateException_WhenMessageNull_ShouldReturnBadRequestWithDefaultMsg() {
        IllegalStateException ex = new IllegalStateException((String) null);
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleIllegalStateException(ex);

        assertAll("Verify response",
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status should be BAD_REQUEST"),
                () -> assertNotNull(response.getBody(), "Response body should not be null"),
                () -> assertEquals("Invalid state request", response.getBody().get("message"), "Message should be default invalid state request"));
    }

    @Test
    void handleIllegalStateException_WhenConflictKeywords_ShouldReturnConflict() {
        IllegalStateException exTergabung = new IllegalStateException("Kamu sudah tergabung");
        ResponseEntity<Map<String, String>> resTergabung = exceptionHandler.handleIllegalStateException(exTergabung);

        IllegalStateException exPenuh = new IllegalStateException("Clan penuh");
        ResponseEntity<Map<String, String>> resPenuh = exceptionHandler.handleIllegalStateException(exPenuh);

        IllegalStateException exAnggota = new IllegalStateException("Bukan anggota");
        ResponseEntity<Map<String, String>> resAnggota = exceptionHandler.handleIllegalStateException(exAnggota);

        assertAll("Verify conflict exceptions return CONFLICT status",
                () -> assertEquals(HttpStatus.CONFLICT, resTergabung.getStatusCode(), "Status should be CONFLICT for 'tergabung'"),
                () -> assertEquals(HttpStatus.CONFLICT, resPenuh.getStatusCode(), "Status should be CONFLICT for 'penuh'"),
                () -> assertEquals(HttpStatus.CONFLICT, resAnggota.getStatusCode(), "Status should be CONFLICT for 'anggota'"));
    }

    @Test
    void handleIllegalStateException_WhenForbiddenKeywords_ShouldReturnForbidden() {
        IllegalStateException exKetua = new IllegalStateException("Hanya ketua yang boleh");
        ResponseEntity<Map<String, String>> resKetua = exceptionHandler.handleIllegalStateException(exKetua);

        IllegalStateException exLeader = new IllegalStateException("Only Leader can change");
        ResponseEntity<Map<String, String>> resLeader = exceptionHandler.handleIllegalStateException(exLeader);

        assertAll("Verify forbidden exceptions return FORBIDDEN status",
                () -> assertEquals(HttpStatus.FORBIDDEN, resKetua.getStatusCode(), "Status should be FORBIDDEN for 'ketua'"),
                () -> assertEquals(HttpStatus.FORBIDDEN, resLeader.getStatusCode(), "Status should be FORBIDDEN for 'Leader'"));
    }

    @Test
    void handleIllegalStateException_WhenOtherMessage_ShouldReturnBadRequest() {
        IllegalStateException ex = new IllegalStateException("generic state error");
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleIllegalStateException(ex);

        assertAll("Verify response",
                () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status should be BAD_REQUEST"),
                () -> assertNotNull(response.getBody(), "Response body should not be null"),
                () -> assertEquals("generic state error", response.getBody().get("message"), "Message should match exception message"));
    }
}
