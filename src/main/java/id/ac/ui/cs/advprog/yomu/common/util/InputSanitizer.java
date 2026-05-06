package id.ac.ui.cs.advprog.yomu.common.util;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * Utility for sanitizing user input to prevent XSS and injection attacks.
 * This component handles HTML encoding and basic input validation.
 */
@Component
public class InputSanitizer {

    private static final int MAX_STRING_LENGTH = 1000;
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "<script>|</script>|<iframe>|</iframe>|javascript:|onerror=|onclick=|onload=",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * Sanitize string input: remove potential XSS vectors, HTML encode special characters, and limit length.
     * 
     * @param input the raw user input string
     * @return sanitized string safe for database storage and frontend rendering
     */
    public String sanitize(String input) {
        if (input == null) {
            return null;
        }
        
        // Trim whitespace
        String sanitized = input.trim();
        
        // Enforce max length before encoding
        if (sanitized.length() > MAX_STRING_LENGTH) {
            sanitized = sanitized.substring(0, MAX_STRING_LENGTH);
        }
        
        // Remove XSS patterns
        sanitized = XSS_PATTERN.matcher(sanitized).replaceAll("");
        
        // HTML encode special characters
        sanitized = sanitized.replace("&", "&amp;")
                             .replace("<", "&lt;")
                             .replace(">", "&gt;")
                             .replace("\"", "&quot;")
                             .replace("'", "&#x27;");
        
        return sanitized;
    }

    /**
     * Validate string length.
     * 
     * @param input the input string to validate
     * @param maxLength maximum allowed length
     * @param fieldName the field name for error messaging
     * @throws IllegalArgumentException if input exceeds max length
     */
    public void validateLength(String input, int maxLength, String fieldName) {
        if (input != null && input.length() > maxLength) {
            throw new IllegalArgumentException(
                fieldName + " exceeds maximum length of " + maxLength
            );
        }
    }

    /**
     * Validate string is not null or empty.
     * 
     * @param input the input string to validate
     * @param fieldName the field name for error messaging
     * @throws IllegalArgumentException if input is null or blank
     */
    public void validateNotEmpty(String input, String fieldName) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }
}
