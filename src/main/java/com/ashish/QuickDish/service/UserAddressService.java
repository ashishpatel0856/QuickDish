package com.ashish.QuickDish.service;

import com.ashish.QuickDish.Entity.User;
import com.ashish.QuickDish.Entity.UserAddress;
import com.ashish.QuickDish.dto.UserAddressRequestDto;
import com.ashish.QuickDish.dto.UserAddressResponseDto;
import com.ashish.QuickDish.exceptions.ResourceNotFoundException;
import com.ashish.QuickDish.repository.UserAddressRepository;
import com.ashish.QuickDish.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public UserAddressResponseDto addAddress(Long userId, UserAddressRequestDto request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserAddress address = new UserAddress();
        address.setAddress(request.getAddress());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        address.setUser(user);

        UserAddress saved = userAddressRepository.save(address);
        return convertToResponse(saved);
    }

    public List<UserAddressResponseDto> getAllAddresses(Long userId) {
        List<UserAddress> addresses = userAddressRepository.findByUserId(userId);
        return addresses
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public void deleteAddress(Long addressId) {
        UserAddress address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        userAddressRepository.delete(address);
    }

    public UserAddressResponseDto updateAddress(Long addressId, UserAddressRequestDto request) {
        UserAddress address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        address.setAddress(request.getAddress());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        return convertToResponse(userAddressRepository.save(address));
    }

    private UserAddressResponseDto convertToResponse(UserAddress address) {
        UserAddressResponseDto response = new UserAddressResponseDto();
        response.setId(address.getId());
        response.setAddress(address.getAddress());
        response.setLatitude(address.getLatitude());
        response.setLongitude(address.getLongitude());
        response.setUserId(address.getUser().getId());
        return response;
    }
}

