package com.ashish.QuickDish.repository;

import com.ashish.QuickDish.Entity.enums.RiderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ashish.QuickDish.Entity.RiderProfile;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RiderProfileRepository extends JpaRepository<RiderProfile, Long> {
    List<RiderProfile> findByIsVerifiedRiderFalse();
    List<RiderProfile> findByIsVerifiedRiderTrue();
    long countByIsVerifiedRiderTrue();
    long countByIsVerifiedRiderFalse();
    Optional<RiderProfile> findByUser_Id(Long userId);
}
