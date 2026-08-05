package com.ashish.QuickDish.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AvailableOrderDto {
    private Long orderId;
    private String restaurantName;
    private String restaurantAddress;
    private String deliveryAddress;
    private Double totalAmount;
    private Integer itemsCount;


    private Double distance;
    private Double deliveryFee;
    private Integer estimatedTime;


    private Double estimatedDistance;
    private Double estimatedEarnings;
}

