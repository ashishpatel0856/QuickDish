package com.ashish.QuickDish.repository;

import com.ashish.QuickDish.Entity.Restaurant;
import com.ashish.QuickDish.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface RestaurantRepository extends JpaRepository<Restaurant,Long> {

    List<Restaurant> findByOwnerId(Long ownerId);
    List<Restaurant> findByApprovedTrue();
    List<Restaurant> findByApprovedFalse();
    List<Restaurant> findByNameContainingIgnoreCase(String name);

    List<Restaurant> findByCategoryContainingIgnoreCase(String category);


    @Query("SELECT r FROM Restaurant r WHERE " +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.category) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.address) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Restaurant> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT r FROM Restaurant r WHERE " +
            "LOWER(r.category) LIKE LOWER(CONCAT('%', :cuisine, '%')) AND " +
            "r.isActive = true AND r.approved = true")
    List<Restaurant> findByCuisineAndActive(@Param("cuisine") String cuisine);
}
