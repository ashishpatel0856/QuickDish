package com.ashish.QuickDish.dto;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto {
    private Long id;
    private int quantity;
    private double price;
    private double totalPrice;
    private FoodItemDto foodItem;
}
