package id.ac.ui.cs.advprog.yomu.auth.controller;

import id.ac.ui.cs.advprog.yomu.auth.dto.AccountResponse;
import id.ac.ui.cs.advprog.yomu.auth.dto.AuthResponse;
import id.ac.ui.cs.advprog.yomu.auth.dto.LinkLoginMethodRequest;
import id.ac.ui.cs.advprog.yomu.auth.dto.LoginRequest;
import id.ac.ui.cs.advprog.yomu.auth.dto.RegisterRequest;
import id.ac.ui.cs.advprog.yomu.auth.dto.UpdateAccountRequest;
import id.ac.ui.cs.advprog.yomu.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AccountResponse> getMe(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(authService.getMe(authentication.getName()));
    }

    @PutMapping("/account")
    public ResponseEntity<AccountResponse> updateAccount(
            Authentication authentication,
            @RequestBody UpdateAccountRequest request) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(authService.updateAccount(authentication.getName(), request));
    }

    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        authService.deleteAccount(authentication.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/account/link")
    public ResponseEntity<AccountResponse> linkLoginMethod(
            Authentication authentication,
            @RequestBody LinkLoginMethodRequest request) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(authService.linkLoginMethod(authentication.getName(), request));
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(new AuthResponse(null, null, null, null, "Logout berhasil"));
    }
}