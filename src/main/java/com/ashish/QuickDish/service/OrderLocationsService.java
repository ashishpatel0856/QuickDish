package com.ashish.QuickDish.service;

import com.ashish.QuickDish.Entity.*;
import com.ashish.QuickDish.exceptions.ResourceNotFoundException;
import com.ashish.QuickDish.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderLocationsService {

    private final OrderAddressRepository orderAddressRepository;
//    private final RestaurantAddressRepository restaurantAddressRepository;
    private final UserAddressRepository userAddressRepository;
    private final RestaurantRepository restaurantRepository;
    private final DeliveryRiderRepository deliveryRiderRepository;
    private final UserRepository userRepository;


    public OrderAddress placeOrder(Long userId, Long restaurantId, Long addressId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("UserId not found"));

        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(() ->
                new ResourceNotFoundException("RestaurantId not found"));

        UserAddress userAddress = userAddressRepository.findById(addressId).orElseThrow(() ->
                new ResourceNotFoundException("AddressId not found"));

        //  Null check karo
        if (restaurant.getLatitude() == null || restaurant.getLongitude() == null) {
            throw new RuntimeException("Restaurant location (latitude/longitude) not set");
        }

        if (userAddress.getLatitude() == null || userAddress.getLongitude() == null) {
            throw new RuntimeException("User address location (latitude/longitude) not set");
        }

        //  Distance calculate karo
        double distanceInKm = CheckNearestDistance.distanceInKm(
                restaurant.getLatitude(),
                restaurant.getLongitude(),
                userAddress.getLatitude(),
                userAddress.getLongitude()
        );

        //  Delivery charges set karo
        double charges = distanceInKm <= 5 ? 20 : 40;

        // Nearest rider
        DeliveryRider nearestRider = findNearestRider(restaurant.getLatitude(), restaurant.getLongitude());

        OrderAddress orderAdrress = new OrderAddress();
        orderAdrress.setUser(user);
        orderAdrress.setRestaurant(restaurant);
        orderAdrress.setUserAddress(userAddress);
        orderAdrress.setRider(nearestRider);
        orderAdrress.setDeliveryCharge(charges);
        orderAdrress.setDeliveryDistance(String.valueOf(distanceInKm));

        return orderAddressRepository.save(orderAdrress);
    }


    private DeliveryRider findNearestRider(double lat, double lon) {
        // Find first rider who distance calculations
        return deliveryRiderRepository
                .findAll()
                .stream().findFirst()
                .orElseThrow(() ->
                new ResourceNotFoundException("No delivery rider found"));
    }
}
