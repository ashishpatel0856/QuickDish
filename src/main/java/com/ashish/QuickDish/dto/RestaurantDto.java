package com.ashish.QuickDish.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantDto {

    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Address is required")
    private String address;

    private String location;

    private Double latitude;

    private Double longitude;

    private String contact;

    @Email(message = "Invalid email format")
    private String email;

    private String[] image;

    public String getImageUrl() {
        if (image != null && image.length > 0) {
            return image[0];
        }
        return null;
    }

    private boolean approved;

    private UserDto owner;

    public String getCuisine() {
        return category;
    }

    public Double getRating() {
        return 4.2;  // Default rating
    }
}