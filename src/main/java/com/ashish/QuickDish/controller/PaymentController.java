package com.ashish.QuickDish.controller;

import com.ashish.QuickDish.Entity.Order;
import com.ashish.QuickDish.service.CheckoutService;
import com.ashish.QuickDish.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final CheckoutService checkoutService;
    private final OrderService orderService;

    public PaymentController(CheckoutService checkoutService, OrderService orderService) {
        this.checkoutService = checkoutService;
        this.orderService = orderService;
    }

    @PostMapping("/init/{orderId}")
    public ResponseEntity<?> initPayments(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        if (order == null || order.getPaid()) {
            return ResponseEntity
                    .badRequest()
                    .body("Invalid order or already paid order");
        }

        String successUrl = "http://localhost:3000/success?orderId=" + orderId;
        String failureUrl = "http://localhost:3000/failure?orderId=" + orderId;

        String sessionUrl = checkoutService.getCheckoutServiceSession(order, successUrl, failureUrl);
        return ResponseEntity.ok(Map.of("sessionUrl", sessionUrl));
    }
}
