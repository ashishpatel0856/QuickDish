package com.ashish.QuickDish.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RiderAssignmentDto {
    private Long assignmentId;
    private Long orderId;
    private String status;
    private String restaurantName;
    private String restaurantAddress;
    private String restaurantPhone;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private Double totalAmount;
    private String pickupOtp;
    private String deliveryOtp;
    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
}
