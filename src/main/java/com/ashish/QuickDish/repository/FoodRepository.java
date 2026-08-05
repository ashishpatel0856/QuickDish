package com.ashish.QuickDish.repository;

import com.ashish.QuickDish.Entity.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<FoodItem, Long> {

    List<FoodItem> findByApprovedTrue();
    List<FoodItem> findByRestaurantIdAndApprovedTrue(Long restaurantId);
    List<FoodItem> findByRestaurantId(Long restaurantId);
    List<FoodItem> findByApprovedFalse();
    List<FoodItem> findByNameContainingIgnoreCaseAndApprovedTrue(String name);
}