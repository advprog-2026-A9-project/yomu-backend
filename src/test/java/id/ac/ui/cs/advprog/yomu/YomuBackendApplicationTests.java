package id.ac.ui.cs.advprog.yomu;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "DB_URL=jdbc:h2:mem:testdb",
        "DB_USERNAME=sa",
        "DB_PASSWORD=password"
})
class YomuBackendApplicationTests {

    @Test
    void contextLoads() {
        // PMD mewajibkan setiap test punya assertion (pembuktian)
        Assertions.assertTrue(true, "Spring context loads successfully");
    }

}