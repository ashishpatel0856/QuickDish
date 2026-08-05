package com.ashish.QuickDish.service;

import com.ashish.QuickDish.Entity.Restaurant;
import com.ashish.QuickDish.Entity.User;
import com.ashish.QuickDish.Entity.enums.Role;
import com.ashish.QuickDish.dto.RestaurantDto;
import com.ashish.QuickDish.dto.UserDto;
import com.ashish.QuickDish.exceptions.ResourceNotFoundException;
import com.ashish.QuickDish.exceptions.UnAuthorisedException;
import com.ashish.QuickDish.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.ashish.QuickDish.utils.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final ModelMapper modelMapper;

    @Value("${app.auto-approve-restaurants:false}")
    private boolean autoApproveRestaurants;

    @Override
    @Transactional
    public RestaurantDto createRestaurant(RestaurantDto restaurantDto) {
        log.info("Creating new restaurant for owner");

        User user = getCurrentUser();
        validateOwnerRole(user);

        Restaurant restaurant = mapToEntity(restaurantDto);
        restaurant.setOwner(user);

        if (autoApproveRestaurants) {
            restaurant.setApproved(true);
            log.info("Restaurant auto-approved (dev mode)");
        } else {
            restaurant.setApproved(false);
            log.info("Restaurant pending admin approval");
        }

        Restaurant saved = restaurantRepository.save(restaurant);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantDto> getAllRestaurants() {
        // Only show approved to public
        return restaurantRepository.findByApprovedTrue()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantDto> getMyRestaurants(Long ownerId) {
        // Owner can see all their restaurants
        User user = getCurrentUser();
        if (!user.getId().equals(ownerId) && !isAdmin(user)) {
            throw new UnAuthorisedException("Cannot view other owner's restaurants");
        }

        return restaurantRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantDto> getPendingRestaurants() {
        validateAdminRole();
        return restaurantRepository.findByApprovedFalse()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RestaurantDto approveRestaurant(Long id) {
        validateAdminRole();

        Restaurant restaurant = findRestaurantById(id);
        restaurant.setApproved(true);

        Restaurant approved = restaurantRepository.save(restaurant);
        log.info("Restaurant approved by admin: {}", id);

        return mapToDto(approved);
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantDto getRestaurantById(Long id) {
        Restaurant restaurant = findRestaurantById(id);
        User user = getCurrentUser();

        boolean isOwner = restaurant.getOwner() != null &&
                restaurant.getOwner().getId().equals(user.getId());
        boolean isAdmin = isAdmin(user);

        if (!isOwner && !isAdmin && !restaurant.isApproved()) {
            throw new UnAuthorisedException("Restaurant not approved yet");
        }

        return mapToDto(restaurant);
    }

    @Override
    @Transactional
    public RestaurantDto updateRestaurantById(Long id, RestaurantDto dto) {
        Restaurant restaurant = findRestaurantById(id);
        User user = getCurrentUser();

        validateOwnerAccess(restaurant, user);

        updateEntityFromDto(restaurant, dto);
        Restaurant updated = restaurantRepository.save(restaurant);

        return mapToDto(updated);
    }

    @Override
    @Transactional
    public void deleteRestaurantById(Long id) {
        Restaurant restaurant = findRestaurantById(id);
        User user = getCurrentUser();

        validateOwnerAccess(restaurant, user);
        restaurantRepository.delete(restaurant);
    }
    private Restaurant findRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));
    }

    private void validateOwnerRole(User user) {
        if (!user.getRoles().contains(Role.ROLE_RESTAURANT_OWNER)) {
            throw new UnAuthorisedException("Only restaurant owners can create restaurants");
        }
    }

    private boolean isAdmin(User user) {
        return user.getRoles().contains(Role.ROLE_ADMIN);
    }

    private void validateAdminRole() {
        User user = getCurrentUser();
        if (!isAdmin(user)) {
            throw new UnAuthorisedException("Only admins can perform this action");
        }
    }

    private void validateOwnerAccess(Restaurant restaurant, User user) {
        boolean isOwner = restaurant.getOwner() != null &&
                restaurant.getOwner().getId().equals(user.getId());
        boolean isAdmin = isAdmin(user);

        if (!isOwner && !isAdmin) {
            throw new UnAuthorisedException("You don't have permission to modify this restaurant");
        }
    }

    //  helper methods
    private Restaurant mapToEntity(RestaurantDto dto) {
        return Restaurant.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .address(dto.getAddress())
                .location(dto.getLocation())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .contact(dto.getContact())
                .email(dto.getEmail())
                .image(dto.getImage())
                .build();
    }

    private void updateEntityFromDto(Restaurant restaurant, RestaurantDto dto) {
        restaurant.setName(dto.getName());
        restaurant.setDescription(dto.getDescription());
        restaurant.setCategory(dto.getCategory());
        restaurant.setAddress(dto.getAddress());
        restaurant.setLocation(dto.getLocation());
        restaurant.setLatitude(dto.getLatitude());
        restaurant.setLongitude(dto.getLongitude());
        restaurant.setContact(dto.getContact());
        restaurant.setEmail(dto.getEmail());
        restaurant.setImage(dto.getImage());
        User user = getCurrentUser();
        if (isAdmin(user) && dto.isApproved()) {
            restaurant.setApproved(true);
        }
    }

    private RestaurantDto mapToDto(Restaurant restaurant) {
        RestaurantDto dto = new RestaurantDto();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setDescription(restaurant.getDescription());
        dto.setCategory(restaurant.getCategory());
        dto.setAddress(restaurant.getAddress());
        dto.setLocation(restaurant.getLocation());
        dto.setLatitude(restaurant.getLatitude());
        dto.setLongitude(restaurant.getLongitude());
        dto.setContact(restaurant.getContact());
        dto.setEmail(restaurant.getEmail());
        dto.setImage(restaurant.getImage());
        dto.setApproved(restaurant.isApproved());

        if (restaurant.getOwner() != null) {
            dto.setOwner(modelMapper.map(restaurant.getOwner(), UserDto.class));
        }

        return dto;
    }
}