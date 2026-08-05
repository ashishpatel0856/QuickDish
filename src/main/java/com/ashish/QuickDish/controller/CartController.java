package com.ashish.QuickDish.controller;

import com.ashish.QuickDish.dto.CartDto;
import com.ashish.QuickDish.dto.AddToCartRequest;
import com.ashish.QuickDish.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDto> getCartByUser(@RequestParam Long userId) {
        CartDto cart = cartService.getOrCreateCart(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping
    public ResponseEntity<CartDto> addToCart(
            @RequestParam Long userId,
            @RequestBody AddToCartRequest request) {
        CartDto cart = cartService.addItemToCart(userId, request);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartDto> updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @RequestParam int quantity) {
        CartDto cart = cartService.updateItemQuantity(cartItemId, quantity);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartDto> removeCartItem(@PathVariable Long cartItemId) {
        CartDto cart = cartService.removeItem(cartItemId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }
}