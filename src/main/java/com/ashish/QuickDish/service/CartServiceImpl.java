package com.ashish.QuickDish.service;

import com.ashish.QuickDish.Entity.Cart;
import com.ashish.QuickDish.Entity.CartItem;
import com.ashish.QuickDish.Entity.FoodItem;
import com.ashish.QuickDish.Entity.User;
import com.ashish.QuickDish.dto.CartDto;
import com.ashish.QuickDish.dto.CartItemDto;
import com.ashish.QuickDish.dto.AddToCartRequest;
import com.ashish.QuickDish.exceptions.ResourceNotFoundException;
import com.ashish.QuickDish.repository.CartRepository;
import com.ashish.QuickDish.repository.CartItemRepository;
import com.ashish.QuickDish.repository.FoodRepository;
import com.ashish.QuickDish.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CartDto getOrCreateCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setTotalAmount(0.0);
                    return cartRepository.save(newCart);
                });

        return convertToDto(cart);
    }

    @Override
    @Transactional
    public CartDto addItemToCart(Long userId, AddToCartRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setTotalAmount(0.0);
                    return cartRepository.save(newCart);
                });

        FoodItem foodItem = foodRepository.findById(request.getFoodItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with id: " + request.getFoodItemId()));

        // Check if already in cart
        CartItem existingItem = cartItemRepository
                .findByCartIdAndFoodItemId(cart.getId(), foodItem.getId())
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            existingItem.setTotalPrice(existingItem.getQuantity() * foodItem.getPrice());
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setFoodItem(foodItem);
            newItem.setQuantity(request.getQuantity());
            newItem.setUnitPrice(foodItem.getPrice());
            newItem.setTotalPrice(request.getQuantity() * foodItem.getPrice());
            newItem.setUserId(userId);
            cartItemRepository.save(newItem);
        }

        updateCartTotal(cart);
        return convertToDto(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public CartDto updateItemQuantity(Long cartItemId, int quantity) {
        if (quantity <= 0) {
            return removeItem(cartItemId);
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        cartItem.setQuantity(quantity);
        cartItem.setTotalPrice(quantity * cartItem.getUnitPrice());
        cartItemRepository.save(cartItem);

        updateCartTotal(cartItem.getCart());
        return convertToDto(cartRepository.findById(cartItem.getCart().getId()).orElseThrow());
    }

    @Override
    @Transactional
    public CartDto removeItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        Cart cart = cartItem.getCart();
        cartItemRepository.delete(cartItem);
        updateCartTotal(cart);

        return convertToDto(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user id: " + userId));

        cartItemRepository.deleteAllByCartId(cart.getId());
        cart.setTotalAmount(0.0);
        cartRepository.save(cart);
    }

    private void updateCartTotal(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        double total = items.stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
        cart.setTotalAmount(total);
        cartRepository.save(cart);
    }

    private CartDto convertToDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser().getId());
        dto.setTotalAmount(cart.getTotalAmount());
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        log.info("Converting cart: {} with {} items", cart.getId(), items.size());

        List<CartItemDto> itemDtos = items.stream().map(item -> {
            CartItemDto itemDto = new CartItemDto();
            itemDto.setId(item.getId());
            itemDto.setFoodItemId(item.getFoodItem().getId());
            itemDto.setFoodName(item.getFoodItem().getName());

            List<String> images = item.getFoodItem().getImages();
            if (images == null || images.isEmpty()) {
                log.warn("No images for food item: {}", item.getFoodItem().getId());
                images = Collections.emptyList();
            }
            itemDto.setFoodImage(images);

            itemDto.setQuantity(item.getQuantity());
            itemDto.setUnitPrice(item.getUnitPrice());
            itemDto.setTotalPrice(item.getTotalPrice());

            log.debug("CartItemDto: food={}, images={}", itemDto.getFoodName(), itemDto.getFoodImage());
            return itemDto;
        }).collect(Collectors.toList());

        dto.setItems(itemDtos);
        dto.setItemCount(itemDtos.size());

        return dto;
    }
}