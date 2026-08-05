package com.ashish.QuickDish.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LocationMessage {
    private Long riderId;
    private Long orderId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
}