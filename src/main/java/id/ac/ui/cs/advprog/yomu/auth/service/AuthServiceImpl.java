package id.ac.ui.cs.advprog.yomu.auth.service;

import id.ac.ui.cs.advprog.yomu.auth.config.JwtUtil;
import id.ac.ui.cs.advprog.yomu.auth.dto.AccountResponse;
import id.ac.ui.cs.advprog.yomu.auth.dto.AuthResponse;
import id.ac.ui.cs.advprog.yomu.auth.dto.LinkLoginMethodRequest;
import id.ac.ui.cs.advprog.yomu.auth.dto.LoginRequest;
import id.ac.ui.cs.advprog.yomu.auth.dto.RegisterRequest;
import id.ac.ui.cs.advprog.yomu.auth.dto.UpdateAccountRequest;
import id.ac.ui.cs.advprog.yomu.auth.event.UserCreatedEvent;
import id.ac.ui.cs.advprog.yomu.auth.event.UserUpdatedEvent;
import id.ac.ui.cs.advprog.yomu.auth.model.User;
import id.ac.ui.cs.advprog.yomu.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import id.ac.ui.cs.advprog.yomu.auth.event.UserDeletedEvent;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final String USER_NOT_FOUND = "Akun tidak ditemukan";

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 20;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public AuthResponse register(RegisterRequest request) {
        final String email = normalize(request.getEmail());
        final String phoneNumber = normalize(request.getPhoneNumber());
        final String username = request.getUsername();

        if (email == null && phoneNumber == null) {
            throw new IllegalArgumentException("Email atau nomor HP harus diisi");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username sudah dipakai");
        }

        if (username.length() < MIN_USERNAME_LENGTH ||
                username.length() > MAX_USERNAME_LENGTH) {
            throw new IllegalArgumentException("Username harus antara 3-20 karakter");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("Username hanya boleh mengandung huruf, angka, dan underscore");
        }
        if (request.getPassword().length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password minimal 8 karakter");
        }

        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Format email tidak valid. Contoh: example@gmail.com");
        }
        if (email != null && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email sudah terdaftar");
        }
        if (phoneNumber != null && userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Nomor HP sudah terdaftar");
        }

        final User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setDisplayName(request.getDisplayName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("PELAJAR");

        final User saved = userRepository.save(user);
        eventPublisher
                .publishEvent(new UserCreatedEvent(this, saved.getId(), saved.getUsername(), saved.getDisplayName()));
        final String token = jwtUtil.generateToken(saved.getId(), saved.getUsername(), saved.getRole());
        return new AuthResponse(saved.getId(), saved.getUsername(), saved.getRole(), token, "Registrasi berhasil");
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        final String identifier = normalize(request.getIdentifier());

        final User user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .or(() -> userRepository.findByEmailIgnoreCase(identifier))
                .or(() -> userRepository.findByPhoneNumber(identifier))
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Password salah");
        }

        final String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new AuthResponse(user.getId(), user.getUsername(), user.getRole(), token, "Login berhasil");
    }

    @Override
    public AccountResponse getMe(String username) {
        final User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));
        return new AccountResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                "OK");
    }

    @Override
    public AccountResponse updateAccount(String username, UpdateAccountRequest request) {
        final User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        final String newDisplayName = normalize(request.getDisplayName());
        final String newPassword = normalize(request.getNewPassword());

        if (newDisplayName != null) {
            user.setDisplayName(newDisplayName);
        }

        if (newPassword != null) {
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Password lama salah");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        final User saved = userRepository.save(user);
        eventPublisher
                .publishEvent(new UserUpdatedEvent(this, saved.getId(), saved.getUsername(), saved.getDisplayName()));
        return new AccountResponse(
                saved.getId(),
                saved.getUsername(),
                saved.getDisplayName(),
                saved.getEmail(),
                saved.getPhoneNumber(),
                saved.getRole(),
                "Akun berhasil diperbarui");
    }

    @Override
    public void deleteAccount(String username) {
        final User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        userRepository.delete(user);
        eventPublisher.publishEvent(new UserDeletedEvent(this, user.getId(), user.getUsername()));

    }

    @Override
    public AccountResponse linkLoginMethod(String username, LinkLoginMethodRequest request) {
        final User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND));

        final String email = normalize(request.getEmail());
        final String phoneNumber = normalize(request.getPhoneNumber());

        if (email != null) {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new IllegalArgumentException("Format email tidak valid");
            }
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email sudah terdaftar");
            }
            user.setEmail(email);
        }

        if (phoneNumber != null) {
            if (userRepository.existsByPhoneNumber(phoneNumber)) {
                throw new IllegalArgumentException("Nomor HP sudah terdaftar");
            }
            user.setPhoneNumber(phoneNumber);
        }

        final User saved = userRepository.save(user);
        return new AccountResponse(
                saved.getId(),
                saved.getUsername(),
                saved.getDisplayName(),
                saved.getEmail(),
                saved.getPhoneNumber(),
                saved.getRole(),
                "Metode login berhasil ditautkan");
    }
}