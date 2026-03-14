package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.ChangePasswordRequest;
import com.hsf.hsf302_ecom.dto.RegisterRequest;
import com.hsf.hsf302_ecom.entity.Users;

public interface UsersService {

    Users register(RegisterRequest request);
    Users authenticate(String email, String rawPassword);
    boolean changePassword(Long userId, ChangePasswordRequest request);

}
