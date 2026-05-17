package id.ac.ui.cs.advprog.yomu;

import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TokenGeneratorTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void cetakTokenAdmin() {
        String token = jwtUtil.generateToken("admin-rahasia-123", "admin", "ADMIN");

        System.out.println("\n==========================================================");
        System.out.println("🔥 TOKEN ADMIN UNTUK POSTMAN 🔥");
        System.out.println(token);
        System.out.println("==========================================================\n");
    }
}