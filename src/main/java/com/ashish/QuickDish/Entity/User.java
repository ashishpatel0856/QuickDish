package com.ashish.QuickDish.Entity;

import com.ashish.QuickDish.Entity.enums.RiderStatus;
import com.ashish.QuickDish.Entity.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;


    private String phone;

    @OneToMany(mappedBy = "customer",fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Order> orders;


    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<Role> roles;

    @Embedded
    private Address address;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Cart cart;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Review> reviews;

    private Boolean isVerified = false;

    // For Restaurant Owners
    @Builder.Default
    private Boolean isApproved = false;


    private String vehicleType;        // BIKE, SCOOTER, CYCLE
    private String licenseNumber;
    private String vehicleNumber;

    @Embedded
    private Location currentLocation;   // For real-time tracking

    private Boolean isOnline = false;   // Rider online status
    private Boolean isVerifiedRider = false; // Admin approval for rider

    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private RiderProfile riderProfile;

    @Enumerated(EnumType.STRING)
    private RiderStatus riderStatus;    // AVAILABLE, BUSY, OFFLINE


;
    // For Restaurant Owners  Documents
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OwnerDocuments ownerDocuments;

    @Column(name = "created_at")
    @Builder.Default
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    public Set<Role> getRoles() {
        return this.roles;
    }

   public boolean isAdmin() {
        return this.roles != null && this.roles.contains(Role.ROLE_ADMIN);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority(r.name()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isVerified != null && this.isVerified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public Boolean getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(Boolean isApproved) {
        this.isApproved = isApproved;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

}