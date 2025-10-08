package com.ashish.QuickDish.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponseDto {

    private Long id;
    private String foodName;
    private Long foodItemId;
    private int quantity;
    private double totalPrice;
    private double unitPrice;
    private Long cartId;

}
