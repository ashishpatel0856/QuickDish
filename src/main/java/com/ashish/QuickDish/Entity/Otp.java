package com.ashish.QuickDish.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    @Column(name = "role")
    private String role;

    @Column(nullable = false)
    private String otp;

    private LocalDateTime otpExpiryTime;


}
