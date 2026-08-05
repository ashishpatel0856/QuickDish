package com.ashish.QuickDish.ai.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ChatResponseDto {
    private String response;
    private String sessionId;
    private List<String> suggestedActions;
    private Integer tokensUsed;
    private LocalDateTime timestamp;
    private Boolean cached;
}