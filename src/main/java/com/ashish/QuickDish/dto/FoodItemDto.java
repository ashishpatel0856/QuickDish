package com.ashish.QuickDish.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodItemDto {

    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private List<String> images;

    public String getImageUrl() {
        if (images != null && !images.isEmpty()) {
            return images.get(0);
        }
        return null;
    }


    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    private Integer quantity;

    private Boolean available;

    private Boolean approved;

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;

    private String category;

    private Integer preparationTime;
}