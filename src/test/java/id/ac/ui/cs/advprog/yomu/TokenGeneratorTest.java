package id.ac.ui.cs.advprog.yomu;

import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class TokenGeneratorTest {

    private static final Logger logger = LoggerFactory.getLogger(TokenGeneratorTest.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void cetakTokenAdmin() {
        String token = jwtUtil.generateToken("admin-rahasia-123", "admin", "ADMIN");

        assertNotNull(token, "Token JWT tidak boleh null");

        logger.info("\n==========================================================");
        logger.info(" TOKEN ADMIN UNTUK POSTMAN ");
        logger.info("\n{}", token);
        logger.info("\n==========================================================\n");
    }
}