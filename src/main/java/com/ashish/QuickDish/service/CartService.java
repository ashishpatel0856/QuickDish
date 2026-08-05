package com.ashish.QuickDish.service;

import com.ashish.QuickDish.dto.CartDto;
import com.ashish.QuickDish.dto.AddToCartRequest;

public interface CartService {
    CartDto getOrCreateCart(Long userId);
    CartDto addItemToCart(Long userId, AddToCartRequest request);
    CartDto updateItemQuantity(Long cartItemId, int quantity);
    CartDto removeItem(Long cartItemId);
    void clearCart(Long userId);
}