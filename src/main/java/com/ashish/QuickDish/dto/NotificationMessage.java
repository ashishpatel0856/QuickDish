package com.ashish.QuickDish.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    private String type;
    private Long orderId;
    private String status;
    private String message;
    private LocalDateTime timestamp;
}
