package com.ashish.QuickDish.service;

import com.ashish.QuickDish.Entity.FoodItem;
import com.ashish.QuickDish.Entity.Restaurant;
import com.ashish.QuickDish.Entity.User;
import com.ashish.QuickDish.Entity.enums.Role;
import com.ashish.QuickDish.dto.FoodItemDto;
import com.ashish.QuickDish.exceptions.ResourceNotFoundException;
import com.ashish.QuickDish.exceptions.UnAuthorisedException;
import com.ashish.QuickDish.repository.FoodRepository;
import com.ashish.QuickDish.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.ashish.QuickDish.utils.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodServiceImpl implements FoodService {

    private final FoodRepository foodRepository;
    private final RestaurantRepository restaurantRepository;

    @Value("${app.auto-approve-foods:false}")
    private boolean autoApproveFoods;

    @Override
    @Transactional
    public FoodItemDto addNweFoodItem(FoodItemDto foodItemDto) {
        log.info("Adding food item for restaurant: {}", foodItemDto.getRestaurantId());

        User user = getCurrentUser();
        Restaurant restaurant = findRestaurantById(foodItemDto.getRestaurantId());

        validateRestaurantOwner(restaurant, user);

        FoodItem foodItem = FoodItem.builder()
                .name(foodItemDto.getName())
                .description(foodItemDto.getDescription())
                .images(foodItemDto.getImages())
                .price(foodItemDto.getPrice())
                .quantity(foodItemDto.getQuantity() != null ? foodItemDto.getQuantity() : 0)
                .available(true)
                .approved(autoApproveFoods)
                .preparationTime(foodItemDto.getPreparationTime())
                .restaurant(restaurant)
                .build();

        FoodItem saved = foodRepository.save(foodItem);
        log.info("Food item created: {} (approved: {})", saved.getId(), saved.getApproved());

        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodItemDto> getAllFoodItems() {
        return foodRepository.findByApprovedTrue()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodItemDto> getApprovedFoodsByRestaurant(Long restaurantId) {
        Restaurant restaurant = findRestaurantById(restaurantId);

        if (!restaurant.isApproved()) {
            return List.of();
        }

        return foodRepository.findByRestaurantIdAndApprovedTrue(restaurantId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodItemDto> getFoodsByRestaurant(Long restaurantId) {
        // Owner can see all their foods
        User user = getCurrentUser();
        Restaurant restaurant = findRestaurantById(restaurantId);

        validateRestaurantOwner(restaurant, user);

        return foodRepository.findByRestaurantId(restaurantId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FoodItemDto getFoodItemById(Long foodId) {
        FoodItem foodItem = findFoodItemById(foodId);

        // If not approved, only owner or admin can see
        if (!foodItem.getApproved()) {
            User user = getCurrentUser();
            boolean isOwner = foodItem.getRestaurant().getOwner().getId().equals(user.getId());
            boolean isAdmin = isAdmin(user);

            if (!isOwner && !isAdmin) {
                throw new UnAuthorisedException("This food item is pending approval");
            }
        }

        return mapToDto(foodItem);
    }

    @Override
    @Transactional
    public FoodItemDto updateFoodItemById(Long foodId, FoodItemDto foodItemDto) {
        log.info("Updating food item: {}", foodId);

        User user = getCurrentUser();
        FoodItem foodItem = findFoodItemById(foodId);

        validateFoodItemOwner(foodItem, user);

        foodItem.setName(foodItemDto.getName());
        foodItem.setDescription(foodItemDto.getDescription());
        foodItem.setImages(foodItemDto.getImages());
        foodItem.setPrice(foodItemDto.getPrice());
        foodItem.setQuantity(foodItemDto.getQuantity());
        foodItem.setAvailable(foodItemDto.getAvailable());
        foodItem.setPreparationTime(foodItemDto.getPreparationTime());

        if (isAdmin(user) && foodItemDto.getApproved() != null) {
            foodItem.setApproved(foodItemDto.getApproved());
        }

        FoodItem updated = foodRepository.save(foodItem);
        return mapToDto(updated);
    }

    @Override
    @Transactional
    public void deleteFoodItemById(Long foodId) {
        log.info("Deleting food item: {}", foodId);

        User user = getCurrentUser();
        FoodItem foodItem = findFoodItemById(foodId);

        validateFoodItemOwner(foodItem, user);

        foodRepository.delete(foodItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodItemDto> searchFoodItemByName(String name) {
        return foodRepository.findByNameContainingIgnoreCaseAndApprovedTrue(name)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodItemDto> getPendingFoods() {
        validateAdminRole();
        return foodRepository.findByApprovedFalse()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FoodItemDto approveFood(Long foodId) {
        validateAdminRole();

        FoodItem food = findFoodItemById(foodId);
        food.setApproved(true);

        FoodItem approved = foodRepository.save(food);
        log.info("Food approved by admin: {}", foodId);

        return mapToDto(approved);
    }

    @Override
    @Transactional
    public FoodItemDto rejectFood(Long foodId) {
        validateAdminRole();

        FoodItem food = findFoodItemById(foodId);
        food.setAvailable(false);

        FoodItem rejected = foodRepository.save(food);
        log.info("Food rejected by admin: {}", foodId);

        return mapToDto(rejected);
    }
    private FoodItem findFoodItemById(Long id) {
        return foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + id));
    }
    private Restaurant findRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));
    }
    private boolean isRestaurantOwner(Restaurant restaurant, User user) {
        return restaurant.getOwner() != null &&
                restaurant.getOwner().getId().equals(user.getId());
    }

    private boolean isAdmin(User user) {
        return user.getRoles().contains(Role.ROLE_ADMIN);
    }

    private void validateRestaurantOwner(Restaurant restaurant, User user) {
        if (!isRestaurantOwner(restaurant, user) && !isAdmin(user)) {
            throw new UnAuthorisedException("You don't own this restaurant");
        }
    }

    private void validateFoodItemOwner(FoodItem foodItem, User user) {
        boolean isOwner = foodItem.getRestaurant().getOwner().getId().equals(user.getId());
        boolean isAdmin = isAdmin(user);

        if (!isOwner && !isAdmin) {
            throw new UnAuthorisedException("Not authorized to modify this food item");
        }
    }

    private void validateAdminRole() {
        User user = getCurrentUser();
        if (!isAdmin(user)) {
            throw new UnAuthorisedException("Only admins can perform this action");
        }
    }

    private FoodItemDto mapToDto(FoodItem foodItem) {
        FoodItemDto dto = new FoodItemDto();
        dto.setId(foodItem.getId());
        dto.setName(foodItem.getName());
        dto.setDescription(foodItem.getDescription());
        dto.setImages(foodItem.getImages());
        dto.setPrice(foodItem.getPrice());
        dto.setQuantity(foodItem.getQuantity());
        dto.setAvailable(foodItem.getAvailable());
        dto.setApproved(foodItem.getApproved());
        dto.setPreparationTime(foodItem.getPreparationTime());

        if (foodItem.getRestaurant() != null) {
            dto.setRestaurantId(foodItem.getRestaurant().getId());
        }

        return dto;
    }
}