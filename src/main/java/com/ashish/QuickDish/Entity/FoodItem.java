package com.ashish.QuickDish.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String[] image;
    private double price;
    private int quantity;
    private boolean available;

    @ManyToOne
    private Restaurant restaurant;

    @ManyToOne
    private Category category;

    @OneToMany(mappedBy = "foodItem")
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "foodItem")
    private List<OrderItem> orderItems;

}
