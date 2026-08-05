package com.ashish.QuickDish.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String address;

    private String location;
    private Double latitude;
    private Double longitude;
    private String contact;
    private String email;


    @ElementCollection
    @CollectionTable(name = "restaurant_images", joinColumns = @JoinColumn(name = "restaurant_id"))
    @Column(name = "image_url")
    private String[] image;


    @Builder.Default
    private boolean approved = true;


    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;  // Can be deactivated by admin

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Builder.Default
    private Double rating = 0.0;

    @Builder.Default
    private Integer totalReviews = 0;

    @Builder.Default
    private Boolean isOpen = false;  // Owner can open/close

}