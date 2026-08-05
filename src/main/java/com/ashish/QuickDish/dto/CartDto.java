package com.ashish.QuickDish.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private Long id;
    private Long userId;
    private List<CartItemDto> items;
    private Double totalAmount;
    private Integer itemCount;
}