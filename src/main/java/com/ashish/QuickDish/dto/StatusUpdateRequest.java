package com.ashish.QuickDish.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    @NotBlank(message = "Status is required")
    private String status;  // "AVAILABLE", "BUSY", "OFFLINE"
}
