package id.ac.ui.cs.advprog.yomu.auth.service;

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

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("rafitest");
        registerRequest.setEmail("rafi@test.com");
        registerRequest.setDisplayName("Rafi Test");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setIdentifier("rafitest");
        loginRequest.setPassword("password123");

        mockUser = new User();
        mockUser.setId("uuid-123");
        mockUser.setUsername("rafitest");
        mockUser.setEmail("rafi@test.com");
        mockUser.setDisplayName("Rafi Test");
        mockUser.setPassword("encoded_password");
        mockUser.setRole("PELAJAR");
    }

    @Test
    void testRegisterSuccess() {
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded_password");
        when(userRepository.save(any())).thenReturn(mockUser);

        final AuthResponse response = authService.register(registerRequest);

        assertNotNull(response, "Response tidak boleh null");
        assertEquals("rafitest", response.getUsername(), "Username harus sesuai");
        assertEquals("PELAJAR", response.getRole(), "Role harus PELAJAR");
    }

    @Test
    void testRegisterFailUsernameExists() {
        when(userRepository.existsByUsername("rafitest")).thenReturn(true);

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
    void testLoginSuccess() {
        when(userRepository.findByUsername("rafitest")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);

        final AuthResponse response = authService.login(loginRequest);

        assertNotNull(response, "Response tidak boleh null");
        assertEquals("rafitest", response.getUsername(), "Username harus sesuai");
    }

    @Test
    void testLoginFailWrongPassword() {
        when(userRepository.findByUsername("rafitest")).thenReturn(Optional.of(mockUser));
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

    @Test
    void testLoginReturnsToken() {
        when(userRepository.findByUsername("rafitest")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);

        final AuthResponse response = authService.login(loginRequest);

        assertNotNull(response.getToken(), "Token tidak boleh null setelah login");
        assertFalse(response.getToken().isEmpty(), "Token tidak boleh kosong");
    }

    @Test
    void testRegisterReturnsToken() {
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded_password");
        when(userRepository.save(any())).thenReturn(mockUser);

        final AuthResponse response = authService.register(registerRequest);

        assertNotNull(response.getToken(), "Token tidak boleh null setelah register");
        assertFalse(response.getToken().isEmpty(), "Token tidak boleh kosong");
    }
}