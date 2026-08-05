package com.ashish.QuickDish.controller;

import com.ashish.QuickDish.Entity.Order;
import com.ashish.QuickDish.Entity.enums.OrderStatus;
import com.ashish.QuickDish.dto.AddOrderDto;
import com.ashish.QuickDish.dto.OrderRequestDto;
import com.ashish.QuickDish.dto.OrderResponseDto;
import com.ashish.QuickDish.service.OrderService;
import com.ashish.QuickDish.service.WebSocketService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders/customers")
public class OrderController {
    private final OrderService orderService;
    private final ModelMapper modelMapper;
    private final WebSocketService webSocketService;

    public OrderController(OrderService orderService,
                           ModelMapper modelMapper,
                           WebSocketService webSocketService) {
        this.orderService = orderService;
        this.modelMapper = modelMapper;
        this.webSocketService = webSocketService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> BookingMyOrders(@RequestBody OrderRequestDto orderRequestDto) {
        OrderResponseDto orderResponseDto = orderService.BookingMyOrders(orderRequestDto);

        // Notify restaurant owner
        webSocketService.notifyRestaurantOwner(
                orderRequestDto.getRestaurantId(),
                "NEW_ORDER",
                orderResponseDto
        );

        return ResponseEntity.ok(orderResponseDto);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getMyAllOrders() {
        List<OrderResponseDto> orderResponseDto = orderService.getMyAllOrders();
        return ResponseEntity.ok(orderResponseDto);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getMyOrdersById(@PathVariable Long orderId) {
        OrderResponseDto orderResponseDto = orderService.getMyOrdersById(orderId);
        return ResponseEntity.ok(orderResponseDto);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> updateMyOrdersStatusById(@PathVariable Long orderId,
                                                                     @RequestBody OrderStatus status) {
        OrderResponseDto orderResponseDto = orderService.updateMyOrdersStatusById(orderId, status);
        return ResponseEntity.ok(orderResponseDto);
    }

    @PostMapping("/add-items")
    public ResponseEntity<OrderResponseDto> addMyOrders(@RequestBody AddOrderDto addOrderDto) {
        OrderResponseDto orderResponseDto = orderService.addMyOrders(addOrderDto);
        return ResponseEntity.ok(orderResponseDto);
    }

    @PostMapping("/{orderId}/verify-payment")
    public ResponseEntity<OrderResponseDto> forceVerifyPayment(@PathVariable Long orderId) {
        try {
            OrderResponseDto order = orderService.getMyOrdersById(orderId);
            if (order.isPaid() || "PAID".equals(order.getPaymentStatus())) {
                return ResponseEntity.ok(order);
            }

            OrderResponseDto updated = orderService.markOrderAsPaid(orderId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<OrderResponseDto>> getRestaurantOrders(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) String status) {
        List<Order> orders = orderService.getRestaurantOrders(restaurantId, status);
        return ResponseEntity.ok(orders.stream()
                .map(order -> modelMapper.map(order, OrderResponseDto.class))
                .collect(Collectors.toList()));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        Order order = orderService.updateStatus(orderId, status);

        //  WebSocket notification
        webSocketService.notifyCustomer(order.getCustomer().getId(), "ORDER_STATUS_UPDATE", order);

        return ResponseEntity.ok(modelMapper.map(order, OrderResponseDto.class));
    }

    @PutMapping("/{orderId}/accept")
    public ResponseEntity<OrderResponseDto> acceptOrder(@PathVariable Long orderId) {
        Order order = orderService.acceptOrder(orderId);

        //  WebSocket notification
        webSocketService.notifyCustomer(order.getCustomer().getId(), "ORDER_ACCEPTED", order);

        return ResponseEntity.ok(modelMapper.map(order, OrderResponseDto.class));
    }

    @PutMapping("/{orderId}/reject")
    public ResponseEntity<OrderResponseDto> rejectOrder(
            @PathVariable Long orderId,
            @RequestParam String reason) {
        Order order = orderService.rejectOrder(orderId, reason);

        //  WebSocket notification
        webSocketService.notifyCustomer(order.getCustomer().getId(), "ORDER_REJECTED", order);

        return ResponseEntity.ok(modelMapper.map(order, OrderResponseDto.class));
    }
}