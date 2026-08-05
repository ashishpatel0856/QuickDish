package com.ashish.QuickDish.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RiderLocationMessage {
    private Long riderId;
    private Long orderId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
}
