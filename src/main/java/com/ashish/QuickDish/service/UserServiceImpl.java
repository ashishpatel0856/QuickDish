package com.ashish.QuickDish.service;

import com.ashish.QuickDish.Entity.User;
import com.ashish.QuickDish.Entity.enums.Role;
import com.ashish.QuickDish.dto.ProfileUpdateRequestDto;
import com.ashish.QuickDish.exceptions.ResourceNotFoundException;
import com.ashish.QuickDish.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.ashish.QuickDish.utils.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;



    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id"));
    }

    @Override
    public void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto) {
        User user = getCurrentUser();
        if(profileUpdateRequestDto.getPhone()!=null) user.setPhone(profileUpdateRequestDto.getPhone());
        if(profileUpdateRequestDto.getAddress()!=null) user.setAddress(profileUpdateRequestDto.getAddress());

        if (profileUpdateRequestDto.getRole() != null) {
            try {
                Role role = Role.valueOf(profileUpdateRequestDto.getRole().toUpperCase());
                user.setRole(Set.of(role));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role: " + profileUpdateRequestDto.getRole());
            }
        }        userRepository.save(user);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(() -> new ResourceNotFoundException("User not found with username"));
    }

}
