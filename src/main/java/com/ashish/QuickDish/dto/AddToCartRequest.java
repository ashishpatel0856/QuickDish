package com.ashish.QuickDish.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {
    private Long foodItemId;
    private Integer quantity;
}