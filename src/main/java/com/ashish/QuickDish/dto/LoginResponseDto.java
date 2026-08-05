package com.ashish.QuickDish.dto;

import com.ashish.QuickDish.Entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    private String accessToken;
    private String refreshToken;

    // User info
    private Long id;
    private String email;
    private String name;
    private String phone;
    private Set<Role> roles;
    private boolean isNewUser;


    // Rider specific
    private Boolean isVerified;
    private Boolean isRider;
    private String riderStatus;
    private Boolean isRiderVerified;
    private String vehicleType;

    // Restaurant Owner fields
    private Boolean isRestaurantOwner;
    private Boolean isOwnerApproved;
    private Boolean documentsUploaded;
    private Boolean documentsVerified;
    private String approvalStatus;  // PENDING, APPROVED, REJECTED
    public LoginResponseDto(String accessToken) {
    }


}