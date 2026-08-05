package com.ashish.QuickDish.dto;

import lombok.*;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDto {
    private Long id;
    private Long foodItemId;
    private String foodName;
    private List<String> foodImage;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private Long restaurantId;
    private String restaurantName;

    public String getFirstImage() {
        return (foodImage != null && !foodImage.isEmpty()) ? foodImage.get(0) : null;
    }

    public List<String> getFoodImage() {
        return foodImage != null ? foodImage : Collections.emptyList();
    }
}