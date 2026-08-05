package com.ashish.QuickDish.controller;

import com.ashish.QuickDish.Entity.User;
import com.ashish.QuickDish.dto.RestaurantDto;
import com.ashish.QuickDish.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.ashish.QuickDish.utils.AppUtils.getCurrentUser;

@RestController
@RequiredArgsConstructor
@RequestMapping("/restaurant")
@Slf4j
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping("/Create")
    @PreAuthorize("hasRole('ROLE_RESTAURANT_OWNER')")
    public ResponseEntity<RestaurantDto> createRestaurant(
            @Valid @RequestBody RestaurantDto restaurantDto) {
        return new ResponseEntity<>(
                restaurantService.createRestaurant(restaurantDto),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<RestaurantDto> getRestaurant(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(
                restaurantService.getRestaurantById(restaurantId)
        );
    }

    @GetMapping
    public ResponseEntity<List<RestaurantDto>> getAllRestaurants() {
        return ResponseEntity.ok(
                restaurantService.getAllRestaurants()
        );
    }

    @PutMapping("/{restId}")
    @PreAuthorize("hasRole('ROLE_RESTAURANT_OWNER')")
    public ResponseEntity<RestaurantDto> updateRestaurantById(
            @PathVariable Long restId,
            @Valid @RequestBody RestaurantDto restaurantDto) {
        return ResponseEntity.ok(
                restaurantService.updateRestaurantById(restId, restaurantDto)
        );
    }

    @DeleteMapping("/{restaurantId}")
    @PreAuthorize("hasRole('ROLE_RESTAURANT_OWNER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteRestaurantById(
            @PathVariable Long restaurantId) {
        restaurantService.deleteRestaurantById(restaurantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-restaurants")
    @PreAuthorize("hasRole('ROLE_RESTAURANT_OWNER')")
    public ResponseEntity<List<RestaurantDto>> getMyRestaurants() {
        User user = getCurrentUser();
        return ResponseEntity.ok(
                restaurantService.getMyRestaurants(user.getId())
        );
    }


    @GetMapping("/pending")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<RestaurantDto>> getPendingRestaurants() {
        return ResponseEntity.ok(restaurantService.getPendingRestaurants());
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestaurantDto> approveRestaurant(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.approveRestaurant(id));
    }
}