package com.ashish.QuickDish.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "food_item_id")
    private FoodItem foodItem;

    private Integer quantity;

    private Double unitPrice;

    private Double totalPrice;

    @Column(name = "user_id")
    private Long userId;
}