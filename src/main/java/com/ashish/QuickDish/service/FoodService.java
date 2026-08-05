package com.ashish.QuickDish.service;

import com.ashish.QuickDish.dto.FoodItemDto;

import java.util.List;

public interface FoodService {

    FoodItemDto addNweFoodItem(FoodItemDto foodItemDto);
    List<FoodItemDto> getAllFoodItems();
    List<FoodItemDto> getApprovedFoodsByRestaurant(Long restaurantId);
    List<FoodItemDto> getFoodsByRestaurant(Long restaurantId);
    FoodItemDto getFoodItemById(Long foodId);
    FoodItemDto updateFoodItemById(Long foodId, FoodItemDto foodItemDto);
    void deleteFoodItemById(Long foodId);
    List<FoodItemDto> searchFoodItemByName(String name);
    List<FoodItemDto> getPendingFoods();
    FoodItemDto approveFood(Long foodId);
    FoodItemDto rejectFood(Long foodId);
}