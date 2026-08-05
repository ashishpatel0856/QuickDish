package com.ashish.QuickDish.service;

import com.ashish.QuickDish.dto.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    // to send notifications customers
    public void notifyCustomer(Long userId, String type, Object data) {
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications",
                new WebSocketMessage(type, data)
        );
    }

    //  to send notifications owners
    public void notifyRestaurantOwner(Long restaurantId, String type, Object data) {
        messagingTemplate.convertAndSend("/topic/restaurant/" + restaurantId,
                new WebSocketMessage(type, data)
        );
    }

    //  admin notifications
    public void broadcast(String destination, Object data) {
        messagingTemplate.convertAndSend(destination, data);
    }

    // Message class
    public record WebSocketMessage(String type, Object data, long timestamp) {
        public WebSocketMessage(String type, Object data) {
            this(type, data, System.currentTimeMillis());
        }
    }
}