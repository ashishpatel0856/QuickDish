package com.ashish.QuickDish.dto;

import com.ashish.QuickDish.Entity.Location;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiderProfileDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String vehicleType;
    private String vehicleNumber;
    private String licenseNumber;
    private Boolean isOnline;
    private String status;
    private Double rating;
    private Integer totalDeliveries;
    private Location currentLocation;

}
