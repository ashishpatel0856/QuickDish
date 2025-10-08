package com.ashish.QuickDish.service;

import com.ashish.QuickDish.Entity.Restaurant;
import com.ashish.QuickDish.Entity.User;
import com.ashish.QuickDish.dto.RestaurantDto;
import com.ashish.QuickDish.dto.UserDto;
import com.ashish.QuickDish.exceptions.ResourceNotFoundException;
import com.ashish.QuickDish.exceptions.UnAuthorisedException;
import com.ashish.QuickDish.repository.RestaurantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.ashish.QuickDish.utils.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantServiceImpl implements RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final ModelMapper modelMapper;
    private final UserService userService;




    @Override
    public RestaurantDto createRestaurant(RestaurantDto restaurantDto) {
        log.info("add new restaurant");

        Restaurant restaurant = modelMapper.map(restaurantDto, Restaurant.class);
        User user = getCurrentUser();
        restaurant.setApproved(false);
        restaurant.setOwner(user);
        restaurant = restaurantRepository.save(restaurant);
        RestaurantDto restaurantDto1= modelMapper.map(restaurant, RestaurantDto.class);
        UserDto userDto = modelMapper.map(user, UserDto.class);
        restaurantDto1.setOwner(userDto);
        return restaurantDto1;

    }

    @Override
    public List<RestaurantDto> getAllRestaurants() {
        log.info("getting all restaurant{}");
     List<Restaurant> restaurants = restaurantRepository.findAll();

     return restaurants
             .stream()
             .map(restaurant -> modelMapper.map(restaurant, RestaurantDto.class))
             .collect(Collectors.toList());
    }

    @Override
    public RestaurantDto getRestaurantById(Long id) {
        log.info("geting restaurant with id {}");
        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Restaurant are  not found with restaurantId"));

        User user = getCurrentUser();
        if (restaurant.getOwner() == null || !restaurant.getOwner().getId().equals(user.getId())) {
            throw new UnAuthorisedException("YOU ARE NOT UNAUTHORIZED PERSON");
        }

        return modelMapper.map(restaurant, RestaurantDto.class);
    }

    @Override
    public RestaurantDto updateRestaurantById(Long restId, RestaurantDto restaurantDto) {
        log.info("update the restaurant by id");

        Restaurant restaurant = restaurantRepository.findById(restId).orElseThrow(() ->
                new ResourceNotFoundException("not found restaurant with id"));

        User user = getCurrentUser();
        if (restaurant.getOwner() == null || !restaurant.getOwner().getId().equals(user.getId())) {
            throw new UnAuthorisedException("YOU ARE NOT ALLOWED");
        }

        restaurant.setName(restaurantDto.getName());
        restaurant.setEmail(restaurantDto.getEmail());
        restaurant.setContact(restaurantDto.getContact());
        restaurant.setDescription(restaurantDto.getDescription());
        restaurant.setCategory(restaurantDto.getCategory());
        restaurant.setLocation(restaurantDto.getLocation());
        restaurant.setImage(restaurantDto.getImage());
        restaurant.setApproved(restaurantDto.isApproved());
        restaurant.setAddress(restaurantDto.getAddress());

        restaurant.setOwner(user);
        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        RestaurantDto restaurantDto1 = modelMapper.map(updatedRestaurant, RestaurantDto.class);
        UserDto userDto = modelMapper.map(user, UserDto.class);
        restaurantDto1.setOwner(userDto);
        return restaurantDto1;
    }


    @Override
    public void deleteRestaurantById(Long id) {
        log.info("Deleting restaurant with id: {}", id);

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant does not exist with id: " + id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(restaurant.getOwner())){
            throw new UnAuthorisedException("this user does not own this hotel with id"+id);
        }
        restaurantRepository.deleteById(id);
    }

}
