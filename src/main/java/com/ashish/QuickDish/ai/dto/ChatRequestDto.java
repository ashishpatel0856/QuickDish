package com.ashish.QuickDish.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatRequestDto {

    @NotBlank(message = "Message cannot be empty")
    @Size(max = 2000, message = "Message too long")
    private String message;

    private String sessionId;
    private Long restaurantId;
    private Long orderId;
}