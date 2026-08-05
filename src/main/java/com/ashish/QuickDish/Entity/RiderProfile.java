package com.ashish.QuickDish.Entity;

import com.ashish.QuickDish.Entity.enums.RiderStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "rider_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RiderProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "vehicle_type")
    private String vehicleType;

    @Column(name = "license_number")
    private String licenseNumber;

    @Column(name = "vehicle_number")
    private String vehicleNumber;

    @Column(name = "phone")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RiderStatus status;

    @Column(name = "is_online")
    private Boolean isOnline = false;

    @Column(name = "is_verified_rider")
    private Boolean isVerifiedRider = false;

    @Column(name = "rating")
    private Double rating = 0.0;

    @Column(name = "total_deliveries")
    private Integer totalDeliveries = 0;

    @Embedded
    private Location currentLocation;

    @OneToMany(mappedBy = "rider", fetch = FetchType.LAZY)
    private List<RiderAssignment> assignments;
}