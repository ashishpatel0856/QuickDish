package com.ashish.QuickDish.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAddressResponseDto {

    private Long id;
    private String address;
    private Double latitude;
    private Double longitude;
    private Long userId;
}
