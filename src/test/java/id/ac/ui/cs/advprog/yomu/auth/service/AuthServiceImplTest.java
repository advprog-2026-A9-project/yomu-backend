package id.ac.ui.cs.advprog.yomu.auth.service;

import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import id.ac.ui.cs.advprog.yomu.auth.dto.AuthResponse;
import id.ac.ui.cs.advprog.yomu.auth.dto.LoginRequest;
import id.ac.ui.cs.advprog.yomu.auth.dto.RegisterRequest;
import id.ac.ui.cs.advprog.yomu.auth.model.User;
import id.ac.ui.cs.advprog.yomu.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String TEST_USERNAME = "mizukitest";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_ENCODED_PASSWORD = "encoded_password";
    private static final String TEST_TOKEN = "mock-jwt-token";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername(TEST_USERNAME);
        registerRequest.setEmail("mizuki@test.com");
        registerRequest.setDisplayName("Mizuki Test");
        registerRequest.setPassword(TEST_PASSWORD);

        loginRequest = new LoginRequest();
        loginRequest.setIdentifier(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);

        mockUser = new User();
        mockUser.setId("uuid-123");
        mockUser.setUsername(TEST_USERNAME);
        mockUser.setEmail("mizuki@test.com");
        mockUser.setDisplayName("Mizuki Test");
        mockUser.setPassword(TEST_ENCODED_PASSWORD);
        mockUser.setRole("PELAJAR");

       
    }

    @Test
    void testRegisterSuccessReturnsUsername() {
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn(TEST_ENCODED_PASSWORD);
        when(userRepository.save(any())).thenReturn(mockUser);
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn(TEST_TOKEN);

        final AuthResponse response = authService.register(registerRequest);

        assertEquals(TEST_USERNAME, response.getUsername(), "Username harus sesuai");
    }

    @Test
    void testRegisterSuccessReturnsRole() {
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn(TEST_ENCODED_PASSWORD);
        when(userRepository.save(any())).thenReturn(mockUser);

        when(jwtUtil.generateToken(any(), any(), any())).thenReturn(TEST_TOKEN);

        final AuthResponse response = authService.register(registerRequest);

        assertEquals("PELAJAR", response.getRole(), "Role harus PELAJAR");
    }

    @Test
    void testRegisterFailUsernameExists() {
        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
            () -> authService.register(registerRequest),
            "Harus throw exception jika username sudah ada");
    }

    @Test
    void testRegisterFailNoEmailAndPhone() {
        registerRequest.setEmail(null);
        registerRequest.setPhoneNumber(null);

        assertThrows(IllegalArgumentException.class,
            () -> authService.register(registerRequest),
            "Harus throw exception jika email dan HP kosong");
    }

    @Test
    void testRegisterReturnsToken() {
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn(TEST_ENCODED_PASSWORD);
        when(userRepository.save(any())).thenReturn(mockUser);

        when(jwtUtil.generateToken(any(), any(), any())).thenReturn(TEST_TOKEN); // ← tambah

        final AuthResponse response = authService.register(registerRequest);

        assertNotNull(response.getToken(), "Token tidak boleh null setelah register");
    }

    @Test
    void testLoginSuccessReturnsUsername() {
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(TEST_PASSWORD, TEST_ENCODED_PASSWORD)).thenReturn(true);
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn(TEST_TOKEN);

        final AuthResponse response = authService.login(loginRequest);

        assertEquals(TEST_USERNAME, response.getUsername(), "Username harus sesuai");
    }

    @Test
    void testLoginReturnsToken() {
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(TEST_PASSWORD, TEST_ENCODED_PASSWORD)).thenReturn(true);
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn(TEST_TOKEN);

        final AuthResponse response = authService.login(loginRequest);

        assertNotNull(response.getToken(), "Token tidak boleh null setelah login");
    }

    @Test
    void testLoginFailWrongPassword() {
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
            () -> authService.login(loginRequest),
            "Harus throw exception jika password salah");
    }

    @Test
    void testLoginFailUserNotFound() {
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> authService.login(loginRequest),
            "Harus throw exception jika user tidak ditemukan");
    }
}