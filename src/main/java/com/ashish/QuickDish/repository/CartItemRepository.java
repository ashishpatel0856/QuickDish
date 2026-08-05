package com.ashish.QuickDish.repository;

import com.ashish.QuickDish.Entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartId(Long cartId);
    Optional<CartItem> findByCartIdAndFoodItemId(Long cartId, Long foodItemId);
    void deleteAllByCartId(Long cartId);
}