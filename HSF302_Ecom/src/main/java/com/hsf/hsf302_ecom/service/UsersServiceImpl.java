package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.ChangePassRequest;
import com.hsf.hsf302_ecom.dto.UserProfileDTO;
import com.hsf.hsf302_ecom.entity.Users;
import com.hsf.hsf302_ecom.repository.UsersRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UsersRepo usersRepo;
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(12);

    @Override
    public Users getUserById(Long id) {
        return usersRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    public UserProfileDTO getProfileByUserId(Long id) {
        Users user = getUserById(id);
        return new UserProfileDTO(
                user.getUsername(),
                user.getEmail(),
                user.getPhone()
        );
    }

    @Override
    public void changePassword(Long userId, ChangePassRequest request) {
        Users user = getUserById(userId);

        if (!ENCODER.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Confirm password does not match");
        }

        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new RuntimeException("New password must be different from old password");
        }

        user.setPassword(ENCODER.encode(request.getNewPassword()));
        usersRepo.save(user);
    }
}
