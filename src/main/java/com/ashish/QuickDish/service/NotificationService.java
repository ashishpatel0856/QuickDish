package com.ashish.QuickDish.service;
import com.ashish.QuickDish.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;


    public void sendToUser(Long userId, String message) {
        try {
            NotificationMessage notification = NotificationMessage.builder()
                    .type("INFO")
                    .message(message)
                    .timestamp(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend("/topic/user/" + userId + "/notifications", notification);
            log.info("Notification sent to user {}: {}", userId, message);
        } catch (Exception e) {
            log.error("Failed to send notification to user {}: {}", userId, e.getMessage());
        }
    }

    public void sendOrderUpdateToUser(Long userId, Long orderId, String status, String message) {
        try {
            NotificationMessage notification = NotificationMessage.builder()
                    .type("ORDER_UPDATE")
                    .orderId(orderId)
                    .status(status)
                    .message(message)
                    .timestamp(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend("/topic/user/" + userId + "/orders", notification);
            log.info("Order update sent to user {} for order {}: {}", userId, orderId, message);
        } catch (Exception e) {
            log.error("Failed to send order update: {}", e.getMessage());
        }
    }



    public void sendToRider(Long riderId, String message) {
        try {
            NotificationMessage notification = NotificationMessage.builder()
                    .type("INFO")
                    .message(message)
                    .timestamp(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend("/topic/rider/" + riderId + "/notifications", notification);
            log.info("Notification sent to rider {}: {}", riderId, message);
        } catch (Exception e) {
            log.error("Failed to send notification to rider {}: {}", riderId, e.getMessage());
        }
    }

    public void sendNewOrderToRider(Long riderId, Long orderId, String message) {
        try {
            NotificationMessage notification = NotificationMessage.builder()
                    .type("NEW_ORDER")
                    .orderId(orderId)
                    .message(message)
                    .timestamp(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend("/topic/rider/" + riderId + "/orders", notification);
            log.info("New order notification sent to rider {}: {}", riderId, message);
        } catch (Exception e) {
            log.error("Failed to send new order notification: {}", e.getMessage());
        }
    }


    public void sendToRestaurant(Long restaurantId, String message) {
        try {
            NotificationMessage notification = NotificationMessage.builder()
                    .type("INFO")
                    .message(message)
                    .timestamp(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend("/topic/restaurant/" + restaurantId + "/notifications", notification);
            log.info("Notification sent to restaurant {}: {}", restaurantId, message);
        } catch (Exception e) {
            log.error("Failed to send notification to restaurant {}: {}", restaurantId, e.getMessage());
        }
    }

    public void sendOrderToRestaurant(Long restaurantId, Long orderId, String message) {
        try {
            NotificationMessage notification = NotificationMessage.builder()
                    .type("NEW_ORDER")
                    .orderId(orderId)
                    .message(message)
                    .timestamp(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend("/topic/restaurant/" + restaurantId + "/orders", notification);
            log.info("New order sent to restaurant {}: {}", restaurantId, message);
        } catch (Exception e) {
            log.error("Failed to send order to restaurant: {}", e.getMessage());
        }
    }


    public void broadcastToAll(String message) {
        try {
            NotificationMessage notification = NotificationMessage.builder()
                    .type("BROADCAST")
                    .message(message)
                    .timestamp(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend("/topic/broadcast", notification);
        } catch (Exception e) {
            log.error("Failed to broadcast: {}", e.getMessage());
        }
    }
}
