package com.ashish.QuickDish.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentId;
    private boolean success;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private LocalDateTime paymentDate;

}
