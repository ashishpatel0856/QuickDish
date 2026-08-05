package com.ashish.QuickDish.controller;

import com.ashish.QuickDish.Entity.User;
import com.ashish.QuickDish.advice.ApiResponse;
import com.ashish.QuickDish.dto.*;
import com.ashish.QuickDish.service.RiderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/riders")
@RequiredArgsConstructor
public class RiderController {

    private final RiderService riderService;

    @PutMapping("/status")
    public ResponseEntity<?> updateStatus(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody StatusUpdateRequest request) {
        riderService.updateStatus(user.getId(), request.getStatus());
        return ResponseEntity.ok(new ApiResponse("Status updated to: " + request.getStatus()));
    }

    @PutMapping("/location")
    public ResponseEntity<?> updateLocation(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody LocationUpdateRequest request) {
        riderService.updateLocation(user.getId(), request);
        return ResponseEntity.ok(new ApiResponse("Location updated"));
    }

    @GetMapping("/orders/available")
    public ResponseEntity<List<AvailableOrderDto>> getAvailableOrders(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(riderService.getAvailableOrders(user.getId()));
    }

    @PostMapping("/orders/{orderId}/accept")
    public ResponseEntity<RiderAssignmentDto> acceptOrder(
            @AuthenticationPrincipal User user,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(riderService.acceptOrder(user.getId(), orderId));
    }

    @GetMapping("/orders/current")
    public ResponseEntity<?> getCurrentOrder(@AuthenticationPrincipal User user) {
        RiderAssignmentDto currentOrder = riderService.getCurrentOrder(user.getId());
        return ResponseEntity.ok(currentOrder);  // null bhi return ho sakta hai
    }

    @PutMapping("/orders/{assignmentId}/arrive-restaurant")
    public ResponseEntity<?> arriveAtRestaurant(
            @AuthenticationPrincipal User user,
            @PathVariable Long assignmentId) {
        riderService.arriveAtRestaurant(user.getId(), assignmentId);
        return ResponseEntity.ok(new ApiResponse("Arrived at restaurant"));
    }

    @PutMapping("/orders/{assignmentId}/pickup")
    public ResponseEntity<?> pickupOrder(
            @AuthenticationPrincipal User user,
            @PathVariable Long assignmentId,
            @Valid @RequestBody OtpVerificationRequest request) {
        riderService.pickupOrder(user.getId(), assignmentId, request.getOtp());
        return ResponseEntity.ok(new ApiResponse("Order picked up successfully"));
    }

    @PutMapping("/orders/{assignmentId}/arrive-customer")
    public ResponseEntity<?> arriveAtCustomer(
            @AuthenticationPrincipal User user,
            @PathVariable Long assignmentId) {
        riderService.arriveAtCustomer(user.getId(), assignmentId);
        return ResponseEntity.ok(new ApiResponse("Arrived at customer location"));
    }

    @PutMapping("/orders/{assignmentId}/deliver")
    public ResponseEntity<?> deliverOrder(
            @AuthenticationPrincipal User user,
            @PathVariable Long assignmentId,
            @Valid @RequestBody OtpVerificationRequest request) {
        riderService.deliverOrder(user.getId(), assignmentId, request.getOtp());
        return ResponseEntity.ok(new ApiResponse("Order delivered successfully"));
    }

    @GetMapping("/orders/history")
    public ResponseEntity<List<RiderAssignmentDto>> getOrderHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(riderService.getOrderHistory(user.getId(), page));
    }

    @GetMapping("/earnings/today")
    public ResponseEntity<EarningsDto> getTodayEarnings(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(riderService.getTodayEarnings(user.getId()));
    }

    @GetMapping("/earnings")
    public ResponseEntity<EarningsDto> getEarnings(
            @AuthenticationPrincipal User user,
            @RequestParam String from,
            @RequestParam String to) {
        return ResponseEntity.ok(riderService.getEarnings(user.getId(), from, to));
    }

    @GetMapping("/profile")
    public ResponseEntity<RiderProfileDto> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(riderService.getProfile(user.getId()));
    }
}