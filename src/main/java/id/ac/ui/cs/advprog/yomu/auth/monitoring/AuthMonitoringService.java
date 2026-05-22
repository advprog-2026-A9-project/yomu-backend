package id.ac.ui.cs.advprog.yomu.auth.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class AuthMonitoringService {

    private static final String AUTH_OPERATION_COUNTER = "yomu_auth_operations_total";
    private static final String AUTH_OPERATION_TIMER = "yomu_auth_operation_duration_seconds";
    private static final String AUTH_JWT_COUNTER = "yomu_auth_jwt_validation_total";
    private static final String AUTH_EVENT_COUNTER = "yomu_auth_events_total";

    private final MeterRegistry meterRegistry;

    public void recordTimedOperation(String operation, boolean success, long durationNanos) {
        String outcome = success ? "success" : "failure";

        meterRegistry.counter(AUTH_OPERATION_COUNTER, "operation", operation, "outcome", outcome).increment();
        Timer.builder(AUTH_OPERATION_TIMER)
                .description("Duration of auth operations")
                .tag("operation", operation)
                .tag("outcome", outcome)
                .register(meterRegistry)
                .record(durationNanos, TimeUnit.NANOSECONDS);
    }

    public void recordJwtValidation(String outcome) {
        meterRegistry.counter(AUTH_JWT_COUNTER, "outcome", outcome).increment();
    }

    public void recordEvent(String event, String outcome) {
        meterRegistry.counter(AUTH_EVENT_COUNTER, "event", event, "outcome", outcome).increment();
    }
}