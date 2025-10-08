package com.ashish.QuickDish.dto;

import com.ashish.QuickDish.Entity.Address;
import com.ashish.QuickDish.Entity.enums.OrderStatus;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDto {

 private Long restaurantId;
 private double totalPrice;
 private String deliveryAddress;
 private String notes;
 private OrderStatus status;
 private List<OrderItemRequestDto> orderItems;

}
