package com.ashish.QuickDish.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private User user;

    @ManyToOne
//    @JoinColumn(name = "restaurant_id", nullable = false)
//    @JsonBackReference
    @JsonIgnore
    private Restaurant restaurant;

    @ManyToOne
    @JsonIgnore
    private UserAddress userAddress;

    @ManyToOne
    @JsonIgnore
    private DeliveryRider rider;

    private Double deliveryCharge;
    private String deliveryDistance;
    private String deliveryDuration;


}
