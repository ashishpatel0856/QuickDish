package com.ashish.QuickDish.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRequestDto {
    private Long foodItemId;
    private int quantity;
}
