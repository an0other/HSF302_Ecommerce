package com.hsf.hsf302_ecom.service;

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
    public boolean authenticate(String email, String rawPassword) {
        Users user = usersRepo.findByEmailIgnoreCase(email);

        if(user == null) return false;

        return passwordEncoder.matches(rawPassword, user.getPassword());
    }
}
