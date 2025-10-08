package com.ashish.QuickDish.dto;

import com.ashish.QuickDish.Entity.CartItem;
import com.ashish.QuickDish.Entity.Category;
import com.ashish.QuickDish.Entity.OrderItem;
import com.ashish.QuickDish.Entity.Restaurant;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodItemDto {
    private Long id;
    private String name;
    private String description;
    private String[] image;
    private double price;
    private int quantity;
    private boolean available;


}
