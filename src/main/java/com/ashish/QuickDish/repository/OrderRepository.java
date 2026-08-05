package com.ashish.QuickDish.repository;
import com.ashish.QuickDish.Entity.Order;
import com.ashish.QuickDish.Entity.User;
import com.ashish.QuickDish.Entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

 List<Order> findAllByCustomer(User user);
 Optional<Order> findByPaymentSessionId(String sessionId);

 @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
 Optional<Order> findByIdWithItems(@Param("id") Long id);

 @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.customer = :customer")
 List<Order> findAllByCustomerWithItems(@Param("customer") User customer);
 List<Order> findByRestaurantIdAndStatus(Long restaurantId, OrderStatus orderStatus);
 List<Order> findByRestaurantId(Long restaurantId);

 @Query(value = """
        SELECT o.* FROM orders o 
        INNER JOIN restaurants r ON o.restaurant_id = r.id
        WHERE o.status IN ('PAID', 'CONFIRMED', 'PREPARING', 'READY_FOR_PICKUP', 'OUT_FOR_DELIVERY')
        AND (o.rider_assigned = false OR o.rider_assigned IS NULL)
        AND r.latitude IS NOT NULL 
        AND r.longitude IS NOT NULL
        AND (6371 * acos(
            cos(radians(:lat)) * cos(radians(r.latitude)) 
            * cos(radians(r.longitude) - radians(:lng)) 
            + sin(radians(:lat)) * sin(radians(r.latitude))
        )) <= :radiusKm
        """, nativeQuery = true)
 List<Order> findNearbyAvailableOrders(
         @Param("lat") Double lat,
         @Param("lng") Double lng,
         @Param("radiusKm") Double radiusKm);
}