package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.RegisterDTO;
import com.hsf.hsf302_ecom.entity.Users;

import java.util.Optional;

public interface AuthService {

    Optional<Users> login(String usernameOrEmail, String rawPassword);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    RegisterResult registerPending(RegisterDTO dto);

    boolean activateAccount(Long userId, String submittedCode, String sessionCode);

    String resendActivationCode(String email);

    Optional<PasswordResetResult> initiatePasswordReset(String identifier);

    void resetPassword(Long userId, String newRawPassword);

    record RegisterResult(Users user, String code) {}
    record PasswordResetResult(Users user, String code) {}
}