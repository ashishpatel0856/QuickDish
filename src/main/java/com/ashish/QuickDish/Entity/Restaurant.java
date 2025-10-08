package com.ashish.QuickDish.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String contact;
    private String location;
    private boolean approved;
    private String description;
    private String category;
    private String[] image;
    private Double latitude;
    private Double longitude;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "restaurant",cascade = CascadeType.ALL)
    private List<FoodItem> foodItemList;

    @OneToMany(mappedBy = "restaurant")
    @JsonIgnore
    private List<Order> orders;


    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Review> reviews;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User Owner;

}
