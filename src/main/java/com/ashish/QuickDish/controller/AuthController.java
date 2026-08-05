package com.ashish.QuickDish.controller;

import com.ashish.QuickDish.Entity.User;
import com.ashish.QuickDish.Entity.enums.Role;
import com.ashish.QuickDish.advice.ApiResponse;
import com.ashish.QuickDish.dto.*;
import com.ashish.QuickDish.security.AuthService;
import com.ashish.QuickDish.security.JWTService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JWTService jwtService;


    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> signUp(@RequestBody SignupDto signUpRequestDto) {
        return new ResponseEntity<>(authService.signUp(signUpRequestDto), HttpStatus.CREATED);
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginDto loginDto, HttpServletResponse response) {

        String[] tokens = authService.login(loginDto);
        String accessToken = tokens[0];
        String refreshToken = tokens[1];

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);

        User user = authService.getUserFromToken(accessToken);

        LoginResponseDto.LoginResponseDtoBuilder responseBuilder = LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .roles(user.getRoles())
                .isVerified(user.getIsVerified())
                .isNewUser(false);

        // Rider fields
        boolean isRider = user.getRoles().contains(Role.ROLE_RIDER);
        responseBuilder.isRider(isRider);

        if (isRider && user.getRiderProfile() != null) {
            responseBuilder
                    .riderStatus(user.getRiderProfile().getStatus() != null ?
                            user.getRiderProfile().getStatus().name() : null)
                    .isRiderVerified(user.getRiderProfile().getIsVerifiedRider())
                    .vehicleType(user.getRiderProfile().getVehicleType());
        }

        // Restaurant Owner fields
        boolean isOwner = user.getRoles().contains(Role.ROLE_RESTAURANT_OWNER);
        responseBuilder.isRestaurantOwner(isOwner);

        if (isOwner) {
            responseBuilder.isOwnerApproved(user.getIsApproved());

            if (user.getOwnerDocuments() != null) {
                responseBuilder
                        .documentsUploaded(user.getOwnerDocuments().getDocumentsUploaded())
                        .documentsVerified(user.getOwnerDocuments().getDocumentsVerified());
            }

            // Approval status message
            String approvalStatus;
            if (user.getIsApproved()) {
                approvalStatus = "APPROVED";
            } else if (user.getOwnerDocuments() != null && user.getOwnerDocuments().getDocumentsUploaded()) {
                approvalStatus = "PENDING_REVIEW";
            } else {
                approvalStatus = "DOCUMENTS_PENDING";
            }
            responseBuilder.approvalStatus(approvalStatus);
        }

        return ResponseEntity.ok(responseBuilder.build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(HttpServletRequest request) {
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new AuthenticationServiceException("Refresh token not found inside the Cookies"));

        String accessToken = authService.refreshToken(refreshToken);
        User user = authService.getUserFromToken(accessToken);

        LoginResponseDto.LoginResponseDtoBuilder responseBuilder = LoginResponseDto.builder()
                .accessToken(accessToken)
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .roles(user.getRoles())
                .isVerified(user.getIsVerified())
                .isNewUser(false);

        boolean isRider = user.getRoles().contains(Role.ROLE_RIDER);
        responseBuilder.isRider(isRider);

        if (isRider && user.getRiderProfile() != null) {
            responseBuilder
                    .isRiderVerified(user.getRiderProfile().getIsVerifiedRider())
                    .riderStatus(user.getRiderProfile().getStatus() != null ?
                            user.getRiderProfile().getStatus().name() : null);
        }

        boolean isOwner = user.getRoles().contains(Role.ROLE_RESTAURANT_OWNER);
        responseBuilder.isRestaurantOwner(isOwner);

        if (isOwner) {
            responseBuilder.isOwnerApproved(user.getIsApproved());
            if (user.getOwnerDocuments() != null) {
                responseBuilder
                        .documentsUploaded(user.getOwnerDocuments().getDocumentsUploaded())
                        .documentsVerified(user.getOwnerDocuments().getDocumentsVerified());
            }
        }

        return ResponseEntity.ok(responseBuilder.build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PostMapping("/otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(@RequestBody OtpRequestDto otpRequestDto) {
        return ResponseEntity.ok(authService.VerifyOtp(otpRequestDto));
    }
}