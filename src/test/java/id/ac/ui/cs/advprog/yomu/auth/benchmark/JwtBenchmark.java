package id.ac.ui.cs.advprog.yomu.auth.benchmark;

import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import org.openjdk.jmh.annotations.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class JwtBenchmark {

    private JwtUtil jwtUtil;
    private String token;

    @Setup
    public void setup() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretString",
            "YomuSuperSecretKeyForLocalTestingWhichIsAtLeast32BytesLong");
        token = jwtUtil.generateToken("uuid-123", "testuser", "PELAJAR");
    }

    @Benchmark
    public String benchmarkGenerateToken() {
        return jwtUtil.generateToken("uuid-123", "testuser", "PELAJAR");
    }

    @Benchmark
    public String benchmarkExtractUsername() {
        return jwtUtil.extractUsername(token);
    }

    @Benchmark
    public boolean benchmarkValidateToken() {
        return jwtUtil.validateToken(token);
    }
}