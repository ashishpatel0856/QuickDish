package com.ashish.QuickDish.controller;

import com.ashish.QuickDish.dto.UserAddressRequestDto;
import com.ashish.QuickDish.dto.UserAddressResponseDto;
import com.ashish.QuickDish.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/address")
public class UserAddressController {
    private final UserAddressService userAddressService;

    @PostMapping("/add")
    public ResponseEntity<UserAddressResponseDto> addAddress(@RequestParam Long userId,
                                                             @RequestBody UserAddressRequestDto request) {
        return ResponseEntity.ok(userAddressService.addAddress(userId, request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserAddressResponseDto>> getUserAddresses(@PathVariable Long userId) {
        return ResponseEntity.ok(userAddressService.getAllAddresses(userId));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<UserAddressResponseDto> updateAddress(@PathVariable Long addressId,
                                                             @RequestBody UserAddressRequestDto request) {
        return ResponseEntity.ok(userAddressService.updateAddress(addressId, request));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long addressId) {
        userAddressService.deleteAddress(addressId);
        return ResponseEntity.ok("Address deleted successfully");
    }
}
