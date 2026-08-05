package com.ashish.QuickDish.service;

import com.ashish.QuickDish.Entity.*;
import com.ashish.QuickDish.Entity.enums.DeliveryStatus;
import com.ashish.QuickDish.Entity.enums.OrderStatus;
import com.ashish.QuickDish.Entity.enums.RiderStatus;
import com.ashish.QuickDish.dto.*;
import com.ashish.QuickDish.repository.*;
import com.ashish.QuickDish.security.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiderService {

    private final UserRepository userRepository;
    private final RiderProfileRepository riderProfileRepository;
    private final OrderRepository orderRepository;
    private final RiderAssignmentRepository assignmentRepository;
    private final ModelMapper modelMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Transactional
    public void updateStatus(Long userId, String status) {
        try {
            RiderProfile rider = getRiderProfile(userId);
            RiderStatus newStatus = RiderStatus.valueOf(status.toUpperCase());

            rider.setStatus(newStatus);

            if (newStatus == RiderStatus.AVAILABLE) {
                rider.setIsOnline(true);
            } else if (newStatus == RiderStatus.OFFLINE) {
                rider.setIsOnline(false);
            }
            riderProfileRepository.save(rider);
            log.info("Rider {} status updated to {}", userId, status);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status + ". Allowed: OFFLINE, AVAILABLE, ONLINE, BUSY");
        }
    }

    @Transactional
    public void updateLocation(Long userId, LocationUpdateRequest request) {
        if (request == null || request.getLatitude() == null || request.getLongitude() == null) {
            throw new RuntimeException("Latitude and longitude are required");
        }

        RiderProfile rider = getRiderProfile(userId);
        Location location = new Location();
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setLastUpdated(LocalDateTime.now());
        rider.setCurrentLocation(location);
        riderProfileRepository.save(rider);

        List<RiderAssignment> assignments = assignmentRepository
                .findByRiderIdAndStatusIn(rider.getId(),
                        List.of(DeliveryStatus.PICKED_UP, DeliveryStatus.ON_THE_WAY));
        if (!assignments.isEmpty()) {
            RiderAssignment currentAssignment = assignments.get(0);

            RiderLocationMessage locationMessage = new RiderLocationMessage(
                    rider.getId(),
                    currentAssignment.getOrder().getId(),
                    request.getLatitude(),
                    request.getLongitude(),
                    LocalDateTime.now()
            );

            messagingTemplate.convertAndSend(
                    "/topic/order/" + currentAssignment.getOrder().getId() + "/tracking",
                    locationMessage
            );

            log.info("Location updated for rider {} on order {}", userId, currentAssignment.getOrder().getId());
        }
    }

    @Transactional(readOnly = true)
    public List<AvailableOrderDto> getAvailableOrders(Long userId) {
        RiderProfile rider = getRiderProfile(userId);

        log.info("Rider {} - Status: {}, isOnline: {}", userId, rider.getStatus(), rider.getIsOnline());

        if (rider.getStatus() != RiderStatus.AVAILABLE) {
            log.warn("Rider {} not available. Current status: {}", userId, rider.getStatus());
            return Collections.emptyList();
        }

        Location riderLoc = rider.getCurrentLocation();
        if (riderLoc == null) {
            log.error(" Rider {} location not set", userId);
            throw new RuntimeException("Please update your location first");
        }

        log.info("Rider {} location: lat={}, lng={}",
                userId, riderLoc.getLatitude(), riderLoc.getLongitude());

        List<Order> availableOrders = orderRepository.findNearbyAvailableOrders(
                riderLoc.getLatitude(),
                riderLoc.getLongitude(),
                5.0 // 5km radius
        );
        log.info("Found {} available orders for rider {}", availableOrders.size(), userId);

        availableOrders.forEach(o -> log.info("  → Order #{} from Restaurant #{}",
                o.getId(), o.getRestaurant().getId()));

        return availableOrders.stream()
                .map(this::mapToAvailableOrderDto)
                .collect(Collectors.toList());
    }



    @Transactional
    public RiderAssignmentDto acceptOrder(Long userId, Long orderId) {
        log.info("acceptOrder called - userId: {}, orderId: {}", userId, orderId);

        RiderProfile rider = getRiderProfile(userId);

        if (rider.getStatus() != RiderStatus.AVAILABLE) {
            throw new RuntimeException("You must be available to accept orders");
        }

        // Check active orders
        List<DeliveryStatus> activeStatuses = List.of(
                DeliveryStatus.ASSIGNED,
                DeliveryStatus.PICKED_UP,
                DeliveryStatus.ON_THE_WAY
        );

        List<RiderAssignment> activeAssignments = assignmentRepository
                .findByRiderIdAndStatusIn(rider.getId(), activeStatuses);

        if (!activeAssignments.isEmpty()) {
            throw new RuntimeException("Complete your current order first");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Generate OTPs
        String pickupOtp = generateOtp();
        String deliveryOtp = generateOtp();

        // Create assignment
        RiderAssignment assignment = new RiderAssignment();
        assignment.setOrder(order);
        assignment.setRider(rider);
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setStatus(DeliveryStatus.ASSIGNED);
        assignment.setPickupOtp(pickupOtp);
        assignment.setDeliveryOtp(deliveryOtp);

        // Update statuses
        rider.setStatus(RiderStatus.BUSY);
        riderProfileRepository.save(rider);

        order.setStatus(OrderStatus.RIDER_ASSIGNED);
        order.setRiderAssigned(true);
        orderRepository.save(order);

        RiderAssignment saved = assignmentRepository.save(assignment);

        // ✅ SEND EMAILS
        try {
            // 1. Pickup OTP to Rider
            emailService.sendPickupOtp(
                    rider.getUser().getEmail(),
                    rider.getUser().getName(),
                    pickupOtp,
                    order.getRestaurant().getName(),
                    order.getRestaurant().getAddress()
            );

            // 2. Delivery OTP to Customer
            emailService.sendDeliveryOtp(
                    order.getUser().getEmail(),
                    order.getUser().getName(),
                    deliveryOtp,
                    rider.getUser().getName(),
                    rider.getPhone()
            );


        } catch (Exception e) {
            log.error(" Failed to send OTP emails: {}", e.getMessage());
        }

        // Notifications
        notificationService.sendOrderUpdateToUser(
                order.getUser().getId(),
                order.getId(),
                "RIDER_ASSIGNED",
                "Rider " + rider.getUser().getName() + " is on the way!"
        );

        notificationService.sendToRestaurant(
                order.getRestaurant().getId(),
                "Rider " + rider.getUser().getName() + " assigned to order #" + orderId
        );

        return mapToRiderAssignmentDto(saved);
    }




    public RiderAssignmentDto getCurrentOrder(Long userId) {
        try {
            RiderProfile rider = getRiderProfile(userId);
            log.info(" Rider found: id={}", rider.getId());

            Optional<RiderAssignment> assignment = assignmentRepository
                    .findTopByRiderIdAndStatusInOrderByAssignedAtDesc(
                            rider.getId(),
                            List.of(DeliveryStatus.ASSIGNED, DeliveryStatus.PICKED_UP, DeliveryStatus.ON_THE_WAY));

            if (assignment.isPresent()) {
                return mapToRiderAssignmentDto(assignment.get());
            } else {
                return null;
            }

        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public void arriveAtRestaurant(Long userId, Long assignmentId) {
        RiderAssignment assignment = getActiveAssignment(userId, assignmentId);

        if (assignment.getStatus() != DeliveryStatus.ASSIGNED) {
            throw new RuntimeException("Invalid order status");
        }

        RiderProfile rider = assignment.getRider();
        Order order = assignment.getOrder();

        //  SEND EMAIL TO RESTAURANT
        try {
            emailService.sendRiderArrivedAtRestaurant(
                    order.getRestaurant().getEmail(),
                    order.getRestaurant().getName(),
                    rider.getUser().getName(),
                    rider.getPhone(),
                    order.getId(),
                    assignment.getPickupOtp()
            );
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
        }

        // Notification
        notificationService.sendToRestaurant(
                order.getRestaurant().getId(),
                " Rider " + rider.getUser().getName() + " has arrived! OTP: " + assignment.getPickupOtp()
        );
    }


    @Transactional
    public void arriveAtCustomer(Long userId, Long assignmentId) {
        RiderAssignment assignment = getActiveAssignment(userId, assignmentId);

        if (assignment.getStatus() != DeliveryStatus.PICKED_UP) {
            throw new RuntimeException("Order not picked up yet");
        }

        assignment.setStatus(DeliveryStatus.ON_THE_WAY);
        assignmentRepository.save(assignment);

        RiderProfile rider = assignment.getRider();
        Order order = assignment.getOrder();

        //  SEND EMAIL TO CUSTOMER
        try {
            emailService.sendRiderArrivedAtCustomer(
                    order.getUser().getEmail(),
                    order.getUser().getName(),
                    rider.getUser().getName(),
                    rider.getPhone(),
                    order.getId(),
                    assignment.getDeliveryOtp()
            );
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
        }

        // Notification
        notificationService.sendOrderUpdateToUser(
                order.getUser().getId(),
                order.getId(),
                "NEARBY",
                "Rider is near your location! OTP: " + assignment.getDeliveryOtp()
        );

    }



    @Transactional
    public void pickupOrder(Long userId, Long assignmentId, String otp) {
        RiderAssignment assignment = getActiveAssignment(userId, assignmentId);

        if (assignment.getStatus() != DeliveryStatus.ASSIGNED) {
            throw new RuntimeException("Order not in assigned status");
        }

        if (!assignment.getPickupOtp().equals(otp)) {
            throw new RuntimeException("Invalid pickup OTP. Please check with restaurant.");
        }

        assignment.setPickedUpAt(LocalDateTime.now());
        assignment.setStatus(DeliveryStatus.PICKED_UP);
        assignmentRepository.save(assignment);

        Order order = assignment.getOrder();
        order.setStatus(OrderStatus.PICKED_UP);
        orderRepository.save(order);

        notificationService.sendOrderUpdateToUser(
                order.getUser().getId(),
                order.getId(),
                "PICKED_UP",
                "Your order has been picked up! " + assignment.getRider().getUser().getName() +
                        " is on the way. Track live location."
        );
    }




    @Transactional
    public void deliverOrder(Long userId, Long assignmentId, String otp) {
        RiderAssignment assignment = getActiveAssignment(userId, assignmentId);

        if (!assignment.getDeliveryOtp().equals(otp)) {
            throw new RuntimeException("Invalid delivery OTP. Please ask customer for correct OTP.");
        }

        assignment.setDeliveredAt(LocalDateTime.now());
        assignment.setStatus(DeliveryStatus.DELIVERED);

        double earnings = calculateEarnings(assignment.getOrder());
        assignment.setEarnings(earnings);
        assignmentRepository.save(assignment);

        Order order = assignment.getOrder();
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        orderRepository.save(order);

        RiderProfile rider = assignment.getRider();
        rider.setStatus(RiderStatus.AVAILABLE);
        rider.setTotalDeliveries(rider.getTotalDeliveries() + 1);
        riderProfileRepository.save(rider);

        notificationService.sendOrderUpdateToUser(
                order.getUser().getId(),
                order.getId(),
                "DELIVERED",
                "Order delivered successfully! Enjoy your meal. Rate your experience."
        );
    }

    public List<RiderAssignmentDto> getOrderHistory(Long userId, int page) {
        List<RiderAssignment> assignments = assignmentRepository
                .findByRiderUserIdAndStatusOrderByDeliveredAtDesc(userId, DeliveryStatus.DELIVERED);
        return assignments.stream()
                .map(this::mapToRiderAssignmentDto)
                .collect(Collectors.toList());
    }


    public EarningsDto getTodayEarnings(Long userId) {
        LocalDate today = LocalDate.now();
        return getEarnings(userId, today.toString(), today.toString());
    }



    public EarningsDto getEarnings(Long userId, String from, String to) {
        LocalDateTime fromDate = LocalDate.parse(from).atStartOfDay();
        LocalDateTime toDate = LocalDate.parse(to).plusDays(1).atStartOfDay();

        List<RiderAssignment> completed = assignmentRepository
                .findByRiderUserIdAndStatusAndDeliveredAtBetween(
                        userId, DeliveryStatus.DELIVERED, fromDate, toDate);

        double totalEarnings = completed.stream()
                .mapToDouble(RiderAssignment::getEarnings)
                .sum();
        int totalDeliveries = completed.size();

        return EarningsDto.builder()
                .totalEarnings(totalEarnings)
                .totalDeliveries(totalDeliveries)
                .from(from)
                .to(to)
                .build();
    }

    public RiderProfileDto getProfile(Long userId) {
        return mapToRiderProfileDto(getRiderProfile(userId));
    }


    private RiderProfile getRiderProfile(Long userId) {
        return riderProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Rider profile not found for user: " + userId));
    }


    private RiderAssignment getActiveAssignment(Long userId, Long assignmentId) {
        RiderAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (!assignment.getRider().getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to this order");
        }

        if (assignment.getStatus() == DeliveryStatus.DELIVERED) {
            throw new RuntimeException("Order already delivered");
        }
        return assignment;
    }


    private String generateOtp() {
        return String.format("%04d", new Random().nextInt(10000));
    }

    private double calculateEarnings(Order order) {
        return Math.max(order.getTotalAmount() * 0.10, 30.0);
    }

    private AvailableOrderDto mapToAvailableOrderDto(Order order) {
        //  Calculate distance
        double distance = 2.5;

        // Calculate earnings (10% of order amount, minimum Rs. 30)
        double earnings = Math.max(order.getTotalAmount() * 0.10, 30.0);
        //  Calculate estimated time (3 min per km + 10 min prep)
        int estimatedTime = (int) (distance * 3 + 10);
        return AvailableOrderDto.builder()
                .orderId(order.getId())
                .restaurantName(order.getRestaurant().getName())
                .restaurantAddress(order.getRestaurant().getAddress())
                .deliveryAddress(order.getDeliveryAddress())
                .totalAmount(order.getTotalAmount())
                .itemsCount(order.getItems().size())

                .distance(distance)
                .deliveryFee(earnings)
                .estimatedTime(estimatedTime)
                .estimatedDistance(calculateDistance(order))
                .estimatedEarnings(Math.max(order.getTotalAmount() * 0.10, 30.0))
                .build();
    }

    private RiderAssignmentDto mapToRiderAssignmentDto(RiderAssignment assignment) {
        return RiderAssignmentDto.builder()
                .assignmentId(assignment.getId())
                .orderId(assignment.getOrder().getId())
                .status(assignment.getStatus().name())
                .restaurantName(assignment.getOrder().getRestaurant().getName())
                .restaurantAddress(assignment.getOrder().getRestaurant().getAddress())
                .customerName(assignment.getOrder().getUser().getName())
                .customerPhone(assignment.getOrder().getUser().getPhone())
                .deliveryAddress(assignment.getOrder().getDeliveryAddress())
                .totalAmount(assignment.getOrder().getTotalAmount())
                .pickupOtp(assignment.getPickupOtp())
                .deliveryOtp(assignment.getDeliveryOtp())
                .assignedAt(assignment.getAssignedAt())
                .pickedUpAt(assignment.getPickedUpAt())
                .deliveredAt(assignment.getDeliveredAt())
                .build();
    }

    private RiderProfileDto mapToRiderProfileDto(RiderProfile profile) {
        return RiderProfileDto.builder()
                .id(profile.getId())
                .name(profile.getUser().getName())
                .email(profile.getUser().getEmail())
                .phone(profile.getPhone())
                .vehicleType(profile.getVehicleType())
                .vehicleNumber(profile.getVehicleNumber())
                .licenseNumber(profile.getLicenseNumber())
                .isOnline(profile.getIsOnline())
                .status(profile.getStatus().name())
                .rating(profile.getRating())
                .totalDeliveries(profile.getTotalDeliveries())
                .currentLocation(profile.getCurrentLocation())
                .build();
    }

    private Double calculateDistance(Order order) {
        return 2.5; // km
    }
}