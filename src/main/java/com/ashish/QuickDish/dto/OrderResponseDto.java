package com.ashish.QuickDish.dto;

import com.ashish.QuickDish.Entity.enums.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {

    private Long id;
    private String restaurantName;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private double totalPrice;
    private boolean isPaid;
    private String deliveryAddress;


}
