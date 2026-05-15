package id.ac.ui.cs.advprog.yomu.gamification.exception;

/**
 * Custom exception for gamification module
 * Follows dependency inversion: high-level modules throw this abstraction,
 * not low-level repository/entity exceptions
 */
public class GamificationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String errorCode;

    public GamificationException(String message) {
        super(message);
        this.errorCode = "GAMIFICATION_ERROR";
    }

    public GamificationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public GamificationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "GAMIFICATION_ERROR";
    }

    public GamificationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
