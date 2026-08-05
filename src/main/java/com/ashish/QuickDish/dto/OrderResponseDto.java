package com.ashish.QuickDish.dto;

import com.ashish.QuickDish.Entity.enums.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {

    private Long id;
    private String orderNumber;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private double totalPrice;
    private String deliveryAddress;
    private String notes;
    private boolean paid;
    private String paymentMethod;
    private String paymentStatus;
    private String paymentUrl;

    private LocalDateTime confirmedAt;
    private LocalDateTime preparingAt;
    private LocalDateTime readyAt;
    private LocalDateTime outForDeliveryAt;
    private LocalDateTime deliveredAt;

    private RestaurantDto restaurant;
    private UserDto customer;
    private List<OrderItemDto> orderItems;



    public void setPaymentUrl(String url) {
        this.paymentUrl = url;
    }


}
