package com.ashish.QuickDish.controller;

import com.ashish.QuickDish.dto.FoodItemDto;
import com.ashish.QuickDish.service.FoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/foods")
@RequiredArgsConstructor
@Slf4j
public class FoodController {

    private final FoodService foodService;

    @GetMapping("/restaurants")
    public ResponseEntity<List<FoodItemDto>> getApprovedFoods() {
        return ResponseEntity.ok(foodService.getAllFoodItems());
    }

    @GetMapping("/restaurant/{restaurantId}/public")
    public ResponseEntity<List<FoodItemDto>> getPublicFoodsByRestaurant(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(foodService.getApprovedFoodsByRestaurant(restaurantId));
    }

    @PostMapping("/restaurants")
    @PreAuthorize("hasRole('ROLE_RESTAURANT_OWNER')")
    public ResponseEntity<FoodItemDto> addNewFoodItem(
            @Valid @RequestBody FoodItemDto foodItemDto) {
        return new ResponseEntity<>(foodService.addNweFoodItem(foodItemDto), HttpStatus.CREATED);
    }

    @GetMapping("/restaurants/my-restaurant/{restaurantId}")
    @PreAuthorize("hasRole('ROLE_RESTAURANT_OWNER')")
    public ResponseEntity<List<FoodItemDto>> getMyFoods(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(foodService.getFoodsByRestaurant(restaurantId));
    }

    @GetMapping("/restaurants/pending")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<FoodItemDto>> getPendingFoods() {
        return ResponseEntity.ok(foodService.getPendingFoods());
    }

    @PatchMapping("/restaurants/{foodId}/approve")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<FoodItemDto> approveFood(@PathVariable Long foodId) {
        return ResponseEntity.ok(foodService.approveFood(foodId));
    }

    @PatchMapping("/restaurants/{foodId}/reject")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<FoodItemDto> rejectFood(@PathVariable Long foodId) {
        return ResponseEntity.ok(foodService.rejectFood(foodId));
    }

    @GetMapping("/restaurants/{foodId}")
    public ResponseEntity<FoodItemDto> getFoodItemById(@PathVariable Long foodId) {
        return ResponseEntity.ok(foodService.getFoodItemById(foodId));
    }

    @PutMapping("/restaurants/{foodId}")
    @PreAuthorize("hasAnyRole('ROLE_RESTAURANT_OWNER', 'ROLE_ADMIN')")
    public ResponseEntity<FoodItemDto> updateFoodItemById(
            @PathVariable Long foodId,
            @Valid @RequestBody FoodItemDto foodItemDto) {
        return ResponseEntity.ok(foodService.updateFoodItemById(foodId, foodItemDto));
    }

    @DeleteMapping("/restaurants/{foodId}")
    @PreAuthorize("hasAnyRole('ROLE_RESTAURANT_OWNER', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteFoodItemById(@PathVariable Long foodId) {
        foodService.deleteFoodItemById(foodId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/restaurants/search")
    public ResponseEntity<List<FoodItemDto>> searchFoodItemByName(
            @RequestParam String name) {
        return ResponseEntity.ok(foodService.searchFoodItemByName(name));
    }
}