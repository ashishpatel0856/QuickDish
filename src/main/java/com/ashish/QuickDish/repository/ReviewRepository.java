package com.ashish.QuickDish.repository;

import com.ashish.QuickDish.Entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
