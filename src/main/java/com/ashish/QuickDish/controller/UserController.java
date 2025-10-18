package com.ashish.QuickDish.controller;

import com.ashish.QuickDish.Entity.User;
import com.ashish.QuickDish.dto.ProfileUpdateRequestDto;
import com.ashish.QuickDish.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {

        this.userService = userService;
    }
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }


    @PatchMapping("/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody ProfileUpdateRequestDto profileUpdateRequestDto){
        userService.updateProfile(profileUpdateRequestDto);
        return ResponseEntity.noContent().build();
    }

}
