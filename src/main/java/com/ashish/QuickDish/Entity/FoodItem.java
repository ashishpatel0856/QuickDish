package com.ashish.QuickDish.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "food_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "food_images", joinColumns = @JoinColumn(name = "food_item_id"))
    @Column(name = "image_url")
    private List<String> images;

    @Column(nullable = false)
    private Double price;

    private Integer quantity;

    @Builder.Default
    private Boolean available = true;

    @Builder.Default
    private Boolean approved = false;

    private Integer preparationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "foodItem", cascade = CascadeType.ALL)
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "foodItem", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    private String imageUrl;

}