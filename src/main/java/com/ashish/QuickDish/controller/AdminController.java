package com.ashish.QuickDish.controller;

import com.ashish.QuickDish.Entity.OwnerDocuments;
import com.ashish.QuickDish.Entity.Restaurant;
import com.ashish.QuickDish.Entity.RiderProfile;
import com.ashish.QuickDish.Entity.User;
import com.ashish.QuickDish.Entity.enums.RiderStatus;
import com.ashish.QuickDish.advice.ApiResponse;
import com.ashish.QuickDish.repository.OwnerDocumentsRepository;
import com.ashish.QuickDish.repository.RestaurantRepository;
import com.ashish.QuickDish.repository.RiderProfileRepository;
import com.ashish.QuickDish.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final RiderProfileRepository riderProfileRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final OwnerDocumentsRepository ownerDocumentsRepository;


    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();

        // Pending approvals
        long pendingRiders = riderProfileRepository.countByIsVerifiedRiderFalse();
        long pendingOwners = userRepository.countByRolesContainingAndIsApprovedFalseAndIsVerifiedTrue(
                com.ashish.QuickDish.Entity.enums.Role.ROLE_RESTAURANT_OWNER
        );
        long pendingDocuments = ownerDocumentsRepository.countByDocumentsVerifiedFalse();

        summary.put("pendingRiders", pendingRiders);
        summary.put("pendingRestaurantOwners", pendingOwners);
        summary.put("pendingDocuments", pendingDocuments);
        summary.put("totalPending", pendingRiders + pendingOwners);

        // Approved stats
        summary.put("approvedRiders", riderProfileRepository.countByIsVerifiedRiderTrue());
        summary.put("approvedOwners", userRepository.countByRolesContainingAndIsApprovedTrue(
                com.ashish.QuickDish.Entity.enums.Role.ROLE_RESTAURANT_OWNER
        ));

        return ResponseEntity.ok(new ApiResponse<>(summary));
    }


    @GetMapping("/riders/pending")
    public ResponseEntity<ApiResponse<List<RiderProfile>>> getPendingRiders() {
        List<RiderProfile> riders = riderProfileRepository.findByIsVerifiedRiderFalse();
        return ResponseEntity.ok(new ApiResponse<>(riders));
    }

    @GetMapping("/riders/approved")
    public ResponseEntity<ApiResponse<List<RiderProfile>>> getApprovedRiders() {
        List<RiderProfile> riders = riderProfileRepository.findByIsVerifiedRiderTrue();
        return ResponseEntity.ok(new ApiResponse<>(riders));
    }

    @GetMapping("/riders")
    public ResponseEntity<ApiResponse<List<RiderProfile>>> getAllRiders() {
        List<RiderProfile> riders = riderProfileRepository.findAll();
        return ResponseEntity.ok(new ApiResponse<>(riders));
    }

    @PutMapping("/riders/{riderId}/approve")
    public ResponseEntity<ApiResponse<String>> approveRider(@PathVariable Long riderId) {

        RiderProfile rider = riderProfileRepository.findById(riderId)
                .orElseThrow(() -> new RuntimeException("Rider profile not found"));

        rider.setIsVerifiedRider(true);
        rider.setStatus(RiderStatus.AVAILABLE);
        riderProfileRepository.save(rider);

        return ResponseEntity.ok(new ApiResponse<>("Rider approved successfully"));
    }

    @PutMapping("/riders/{riderId}/reject")
    public ResponseEntity<ApiResponse<String>> rejectRider(
            @PathVariable Long riderId,
            @RequestParam(required = false) String reason) {
        RiderProfile rider = riderProfileRepository.findById(riderId)
                .orElseThrow(() -> new RuntimeException("Rider not found"));
        riderProfileRepository.deleteById(riderId);

        // TODO: Send rejection email with reason

        return ResponseEntity.ok(new ApiResponse<>("Rider rejected" + (reason != null ? ": " + reason : "")));
    }

    // restaurant owner management
    @GetMapping("/owners/pending")
    public ResponseEntity<ApiResponse<List<User>>> getPendingRestaurantOwners() {
        List<User> owners = userRepository.findByRolesContainingAndIsApprovedFalseAndIsVerifiedTrue(
                com.ashish.QuickDish.Entity.enums.Role.ROLE_RESTAURANT_OWNER
        );
        return ResponseEntity.ok(new ApiResponse<>(owners));
    }

    @GetMapping("/owners/approved")
    public ResponseEntity<ApiResponse<List<User>>> getApprovedRestaurantOwners() {
        List<User> owners = userRepository.findByRolesContainingAndIsApprovedTrue(
                com.ashish.QuickDish.Entity.enums.Role.ROLE_RESTAURANT_OWNER
        );
        return ResponseEntity.ok(new ApiResponse<>(owners));
    }

    @GetMapping("/owners/{ownerId}/documents")
    public ResponseEntity<ApiResponse<OwnerDocuments>> getOwnerDocuments(@PathVariable Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        OwnerDocuments documents = ownerDocumentsRepository.findByUser(owner)
                .orElseThrow(() -> new RuntimeException("Documents not found"));

        return ResponseEntity.ok(new ApiResponse<>(documents));
    }

    @PutMapping("/owners/{ownerId}/approve")
    public ResponseEntity<ApiResponse<String>> approveRestaurantOwner(@PathVariable Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        if (!owner.getRoles().contains(com.ashish.QuickDish.Entity.enums.Role.ROLE_RESTAURANT_OWNER)) {
            throw new RuntimeException("User is not a restaurant owner");
        }
        // Verify documents first
        OwnerDocuments documents = ownerDocumentsRepository.findByUser(owner)
                .orElseThrow(() -> new RuntimeException("Documents not uploaded"));

        if (!documents.getDocumentsVerified()) {
            throw new RuntimeException("Please verify documents first");
        }

        owner.setIsApproved(true);
        userRepository.save(owner);

        // TODO: Send approval email

        return ResponseEntity.ok(new ApiResponse<>("Restaurant owner approved successfully. They can now add restaurants."));
    }

    @PutMapping("/owners/{ownerId}/verify-documents")
    public ResponseEntity<ApiResponse<String>> verifyOwnerDocuments(
            @PathVariable Long ownerId,
            @RequestParam(required = false, defaultValue = "true") boolean approved,  //  default true
            @RequestParam(required = false) String rejectionReason) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        OwnerDocuments documents = ownerDocumentsRepository.findByUser(owner)
                .orElseThrow(() -> new RuntimeException("Documents not found"));

        if (approved) {
            documents.setDocumentsVerified(true);
            documents.setRejectionReason(null);
            ownerDocumentsRepository.save(documents);

            // Auto-approve owner if documents verified
            owner.setIsApproved(true);
            userRepository.save(owner);

            return ResponseEntity.ok(new ApiResponse<>("Documents verified and owner approved"));
        } else {
            documents.setDocumentsVerified(false);
            documents.setRejectionReason(rejectionReason);
            ownerDocumentsRepository.save(documents);

            return ResponseEntity.ok(new ApiResponse<>("Documents rejected: " + rejectionReason));
        }
    }

    @DeleteMapping("/owners/{ownerId}/reject")
    public ResponseEntity<ApiResponse<String>> rejectRestaurantOwner(
            @PathVariable Long ownerId,
            @RequestParam(required = false) String reason) {

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        ownerDocumentsRepository.deleteByUser(owner);
        userRepository.delete(owner);

        // TODO: Send rejection email

        return ResponseEntity.ok(new ApiResponse<>("Restaurant owner rejected" + (reason != null ? ": " + reason : "")));
    }

    // restaurant managements
    @GetMapping("/restaurants")
    public ResponseEntity<ApiResponse<List<Restaurant>>> getAllRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        return ResponseEntity.ok(new ApiResponse<>(restaurants));
    }

    @PutMapping("/restaurants/{restaurantId}/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateRestaurant(@PathVariable Long restaurantId) {

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        restaurant.setIsActive(false);
        restaurantRepository.save(restaurant);

        return ResponseEntity.ok(new ApiResponse<>("Restaurant deactivated"));
    }

    @PutMapping("/restaurants/{restaurantId}/activate")
    public ResponseEntity<ApiResponse<String>> activateRestaurant(@PathVariable Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        restaurant.setIsActive(true);
        restaurantRepository.save(restaurant);

        return ResponseEntity.ok(new ApiResponse<>("Restaurant activated"));
    }

    // earnings
    @GetMapping("/earnings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPlatformEarnings() {
        Map<String, Object> stats = new HashMap<>();

        // Count stats
        long totalRiders = riderProfileRepository.count();
        long approvedRiders = riderProfileRepository.countByIsVerifiedRiderTrue();
        long pendingRiders = riderProfileRepository.countByIsVerifiedRiderFalse();

        long totalOwners = userRepository.countByRolesContaining(
                com.ashish.QuickDish.Entity.enums.Role.ROLE_RESTAURANT_OWNER
        );
        long approvedOwners = userRepository.countByRolesContainingAndIsApprovedTrue(
                com.ashish.QuickDish.Entity.enums.Role.ROLE_RESTAURANT_OWNER
        );

        long totalRestaurants = restaurantRepository.count();

        //  earnings calculation
        double totalEarnings = (approvedRiders * 500) + (approvedOwners * 2000) + (totalRestaurants * 100);

        stats.put("totalEarnings", totalEarnings);
        stats.put("totalRiders", totalRiders);
        stats.put("approvedRiders", approvedRiders);
        stats.put("pendingRiders", pendingRiders);
        stats.put("totalOwners", totalOwners);
        stats.put("approvedOwners", approvedOwners);
        stats.put("totalRestaurants", totalRestaurants);
        stats.put("currency", "INR");

        return ResponseEntity.ok(new ApiResponse<>(stats));
    }
}