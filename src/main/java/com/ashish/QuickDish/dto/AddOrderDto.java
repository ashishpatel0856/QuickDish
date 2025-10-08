package com.ashish.QuickDish.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddOrderDto {

    private Long orderId;
    private List<OrderItemRequestDto> items;

}
