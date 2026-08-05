package com.ashish.QuickDish.repository;

import com.ashish.QuickDish.Entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>{
}
