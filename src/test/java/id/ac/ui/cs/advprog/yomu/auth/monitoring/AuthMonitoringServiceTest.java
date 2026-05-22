package id.ac.ui.cs.advprog.yomu.auth.monitoring;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthMonitoringServiceTest {

    private SimpleMeterRegistry meterRegistry;
    private AuthMonitoringService authMonitoringService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        authMonitoringService = new AuthMonitoringService(meterRegistry);
    }

    @Test
    void recordTimedOperationShouldIncrementCounter() {
        authMonitoringService.recordTimedOperation("login", true, 1_000_000L);

        assertEquals(1.0,
                meterRegistry.counter("yomu_auth_operations_total", "operation", "login", "outcome", "success").count());
    }

    @Test
    void recordJwtValidationShouldIncrementCounter() {
        authMonitoringService.recordJwtValidation("missing");

        assertEquals(1.0,
                meterRegistry.counter("yomu_auth_jwt_validation_total", "outcome", "missing").count());
    }
}