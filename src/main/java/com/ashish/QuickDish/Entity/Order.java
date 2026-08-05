package com.ashish.QuickDish.Entity;

import com.ashish.QuickDish.Entity.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime orderDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @JsonBackReference
    private User customer;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private double totalPrice;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;

    private String deliveryAddress;

    private String notes;

    private boolean isPaid = false;


    private String paymentSessionId;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(unique = true)
    private String orderNumber;

    private LocalDateTime confirmedAt;
    private LocalDateTime preparingAt;
    private LocalDateTime readyAt;
    private LocalDateTime outForDeliveryAt;
    private LocalDateTime deliveredAt;

    private boolean paid;
    @Column(nullable = true)
    @Builder.Default
    private boolean active = true;

    private String paymentMethod;
    private String paymentStatus;

    @Column(nullable = false)
    @Builder.Default
    private double additionalAmount = 0.0;

    // Order.java
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> orderItems = new ArrayList<>();





    // Restaurant location for pickup
    private Double restaurantLat;
    private Double restaurantLng;

    // Customer location for delivery
    private Double deliveryLat;
    private Double deliveryLng;

    // Rename ya alias add karo
    public User getUser() {
        return this.customer;  // customer ko user ke naam se access
    }

    public Double getTotalAmount() {
        return this.totalPrice;  // totalPrice ko totalAmount ke naam se
    }

    public List<OrderItem> getItems() {
        return this.orderItems;
    }

    // Rider assignment ke liye
    @Column(name = "rider_assigned")
    @Builder.Default
    private Boolean riderAssigned = false;

    public Boolean getRiderAssigned() {
        return riderAssigned;
    }

    public void setRiderAssigned(Boolean riderAssigned) {
        this.riderAssigned = riderAssigned;
    }

    // Getter for orderItems (already hai but check karo)
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }


    public boolean isPaid() {
        return paid;
    }


    public boolean getPaid() {
        return isPaid;
    }

    public void setPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }

}
