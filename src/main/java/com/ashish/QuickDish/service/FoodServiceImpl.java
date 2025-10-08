package com.ashish.QuickDish.service;

import com.ashish.QuickDish.Entity.FoodItem;
import com.ashish.QuickDish.dto.FoodItemDto;
import com.ashish.QuickDish.exceptions.ResourceNotFoundException;
import com.ashish.QuickDish.repository.FoodRepository;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodServiceImpl implements FoodService {
    private final FoodRepository foodRepository;
    private final ModelMapper modelMapper;



    @Override
    public FoodItemDto addNweFoodItem(FoodItemDto foodItemDto) {
        log.info("add new food items in restaurant");
        FoodItem foodItem = modelMapper.map(foodItemDto, FoodItem.class);
        foodItem.setAvailable(true);
        foodItem = foodRepository.save(foodItem);
        return modelMapper.map(foodItem, FoodItemDto.class);
    }

    @Override
    public FoodItemDto getFoodItemById(Long foodId) {
        log.info("geting food with foodid: {}", foodId);
        FoodItem foodItem = foodRepository.findById(foodId).orElseThrow(() ->
                new ResourceNotFoundException("food items are not available with foodId"));

        return modelMapper.map(foodItem,FoodItemDto.class);
    }

    @Override
    public List<FoodItemDto> getAllFoodItems() {
        log.info("getting all food items");
        List<FoodItem> foodItems = foodRepository.findAll();
        return foodItems
                .stream()
                .map(ele ->modelMapper.map(ele,FoodItemDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public FoodItemDto updateFoodItemById(Long foodId, FoodItemDto foodItemDto) {
        log.info("updating food with foodid: {}", foodId);
        FoodItem foodItem = foodRepository.findById(foodId).orElseThrow(() ->
                new ResourceNotFoundException("food items are not available with foodId"));
        modelMapper.map(foodItemDto, foodItem);
        foodItem.setId(foodId);
        foodItem.setAvailable(foodItemDto.isAvailable());
        foodItem = foodRepository.save(foodItem);
        return modelMapper.map(foodItem,FoodItemDto.class);
    }

    @Override
    public void deleteFoodItemById(Long foodId) {
        log.info("deleting food with foodid: {}", foodId);
       FoodItem foodItem = foodRepository.findById(foodId).orElseThrow(() ->
               new ResourceNotFoundException("food items are not available with foodId"));
       foodRepository.delete(foodItem);

    }

    @Override
    public List<FoodItemDto> searchFoodItemByName(String name) {
        log.info("searching food with foodname: {}", name);
        return foodRepository.findByNameContainingIgnoreCase(name).stream()
                .map(foodItem -> modelMapper.map(foodItem,FoodItemDto.class))
                .collect(Collectors.toList());
    }
}
