package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.RegisterRequest;
import com.hsf.hsf302_ecom.entity.Users;

public interface UsersService {

    Users register(RegisterRequest request);
    boolean authenticate(String email, String rawPassword);

}
