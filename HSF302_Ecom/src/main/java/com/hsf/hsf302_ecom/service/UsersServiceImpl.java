package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.ChangePasswordRequest;
import com.hsf.hsf302_ecom.dto.RegisterRequest;
import com.hsf.hsf302_ecom.entity.Users;
import com.hsf.hsf302_ecom.enums.UserRole;
import com.hsf.hsf302_ecom.enums.UserStatus;
import com.hsf.hsf302_ecom.repository.UsersRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UsersRepo usersRepo;
    private final PasswordEncoder passwordEncoder;
    @Override
    public Users register(RegisterRequest request) {

        if(usersRepo.existsByEmail(request.getEmail()))
        {
            throw new RuntimeException("Email đã tồn tại");
        }

        if(!request.getPassword().equals(request.getConfirmPassword())){
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }

        Users user = Users.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        return usersRepo.save(user);

    }

    @Override
    public Users authenticate(String email, String rawPassword) {
        Users user = usersRepo.findByEmailIgnoreCase(email);

        if (user == null) return null;

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            return null;
        }

        return user;
    }

    @Override
    public boolean changePassword(Long userId, ChangePasswordRequest request) {
        Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // check old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }

        // check confirm password
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Xác nhận mật khẩu không khớp");
        }

        // encode password mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        usersRepo.save(user);

        return true;
    }


}
