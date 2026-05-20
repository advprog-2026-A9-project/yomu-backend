package id.ac.ui.cs.advprog.yomu.auth.service;

import id.ac.ui.cs.advprog.yomu.auth.dto.AccountResponse;
import id.ac.ui.cs.advprog.yomu.auth.dto.AuthResponse;
import id.ac.ui.cs.advprog.yomu.auth.dto.LinkLoginMethodRequest;
import id.ac.ui.cs.advprog.yomu.auth.dto.LoginRequest;
import id.ac.ui.cs.advprog.yomu.auth.dto.RegisterRequest;
import id.ac.ui.cs.advprog.yomu.auth.dto.UpdateAccountRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AccountResponse getMe(String username);
    AccountResponse updateAccount(String username, UpdateAccountRequest request);
    void deleteAccount(String userId);
    AccountResponse linkLoginMethod(String userId, LinkLoginMethodRequest request);

}