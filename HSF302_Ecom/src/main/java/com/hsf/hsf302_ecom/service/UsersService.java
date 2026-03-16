package com.hsf.hsf302_ecom.service;

import com.hsf.hsf302_ecom.dto.ChangePassRequest;
import com.hsf.hsf302_ecom.dto.UserProfileDTO;
import com.hsf.hsf302_ecom.entity.Users;

public interface UsersService {

    Users getUserById(Long id);
    UserProfileDTO getProfileByUserId(Long id);
    void changePassword(Long userId, ChangePassRequest request);


}
