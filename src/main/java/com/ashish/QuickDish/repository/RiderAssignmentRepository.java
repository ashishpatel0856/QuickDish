package com.ashish.QuickDish.repository;

import com.ashish.QuickDish.Entity.RiderAssignment;
import com.ashish.QuickDish.Entity.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RiderAssignmentRepository extends JpaRepository<RiderAssignment, Long> {

    List<RiderAssignment> findByRiderIdAndStatusIn(Long riderId, List<DeliveryStatus> statuses);
    Optional<RiderAssignment> findTopByRiderIdAndStatusInOrderByAssignedAtDesc(
            Long riderId, List<DeliveryStatus> statuses);

    List<RiderAssignment> findByRiderUserIdAndStatusOrderByDeliveredAtDesc(Long userId, DeliveryStatus status);

    List<RiderAssignment> findByRiderUserIdAndStatusAndDeliveredAtBetween(
            Long userId, DeliveryStatus status, LocalDateTime from, LocalDateTime to);
}