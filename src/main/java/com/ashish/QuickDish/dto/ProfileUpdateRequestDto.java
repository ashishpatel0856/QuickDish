package com.ashish.QuickDish.dto;

import com.ashish.QuickDish.Entity.Address;
import com.ashish.QuickDish.Entity.enums.Role;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfileUpdateRequestDto {

    private String phone;
    private Boolean isVerified;
    @Embedded
    private Address address;
    private String role;


}
