package com.ashish.QuickDish.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAddressRequestDto {

    private String address;
    private Double latitude;
    private Double longitude;
}
