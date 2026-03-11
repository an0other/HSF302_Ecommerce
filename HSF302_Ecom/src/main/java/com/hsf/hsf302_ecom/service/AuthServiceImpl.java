package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.RegisterDTO;
import com.hsf.hsf302_ecom.entity.Users;
import com.hsf.hsf302_ecom.enums.UserStatus;
import com.hsf.hsf302_ecom.enums.UserRole;
import com.hsf.hsf302_ecom.repository.UsersRepo;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(12);

    private final UsersRepo    usersRepo;
    private final EmailService emailService;

    public AuthServiceImpl(UsersRepo usersRepo, EmailService emailService) {
        this.usersRepo    = usersRepo;
        this.emailService = emailService;
    }

    private static String generateCode() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }

    @Override
    public Optional<Users> login(String usernameOrEmail, String rawPassword) {
        Optional<Users> user = usersRepo.findByUsernameAndStatus(usernameOrEmail, UserStatus.ACTIVE);
        if (user.isEmpty()) {
            user = usersRepo.findByEmailAndStatus(usernameOrEmail, UserStatus.ACTIVE);
        }
        return user.filter(u -> ENCODER.matches(rawPassword, u.getPassword()));
    }

    @Override
    public boolean existsByUsername(String username) {
        return usersRepo.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return usersRepo.existsByEmail(email);
    }

    @Override
    public RegisterResult registerPending(RegisterDTO dto) {
        Users user = Users.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(ENCODER.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .role(UserRole.CUSTOMER)
                .status(UserStatus.INACTIVE)
                .build();
        Users saved = usersRepo.save(user);

        String code = generateCode();
        emailService.sendActivationCode(saved.getEmail(), code);
        return new RegisterResult(saved, code);
    }

    @Override
    public boolean activateAccount(Long userId, String submittedCode, String sessionCode) {
        if (sessionCode == null || !sessionCode.equals(submittedCode)) return false;
        return usersRepo.findById(userId).map(user -> {
            user.setStatus(UserStatus.ACTIVE);
            usersRepo.save(user);
            return true;
        }).orElse(false);
    }

    @Override
    public String resendActivationCode(String email) {
        String code = generateCode();
        emailService.sendActivationCode(email, code);
        return code;
    }

    @Override
    public Optional<PasswordResetResult> initiatePasswordReset(String identifier) {
        Optional<Users> user = usersRepo.findByEmailAndStatus(identifier, UserStatus.ACTIVE);
        if (user.isEmpty()) {
            user = usersRepo.findByUsernameAndStatus(identifier, UserStatus.ACTIVE);
        }
        return user.map(u -> {
            String code = generateCode();
            emailService.sendPasswordResetCode(u.getEmail(), code);
            return new PasswordResetResult(u, code);
        });
    }

    @Override
    public void resetPassword(Long userId, String newRawPassword) {
        usersRepo.findById(userId).ifPresent(user -> {
            user.setPassword(ENCODER.encode(newRawPassword));
            usersRepo.save(user);
        });
    }
}