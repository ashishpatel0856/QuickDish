package com.ashish.QuickDish.service;

import com.ashish.QuickDish.Entity.User;
import com.ashish.QuickDish.dto.ProfileUpdateRequestDto;

public interface UserService {
    User getUserById(Long userId);
    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);
}
