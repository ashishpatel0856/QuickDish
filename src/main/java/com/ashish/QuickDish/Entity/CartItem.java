package com.ashish.QuickDish.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;
    private double unitPrice;


    private double totalPrice;;

    @ManyToOne
    @JsonIgnore
    private Cart cart;

    @ManyToOne
    private FoodItem foodItem;

    private Long userId;




}
