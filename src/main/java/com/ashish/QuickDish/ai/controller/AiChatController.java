package com.ashish.QuickDish.ai.controller;

import com.ashish.QuickDish.ai.dto.ChatRequestDto;
import com.ashish.QuickDish.ai.dto.ChatResponseDto;
import com.ashish.QuickDish.ai.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
@Tag(name = "AI Chatbot", description = "AI-powered food assistant")
public class AiChatController {

    private final AiService aiService;

    @PostMapping
    @Operation(summary = "Chat with QuickDish AI")
    public ResponseEntity<ChatResponseDto> chat(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody ChatRequestDto request) {

        ChatResponseDto response = aiService.chat(userId, request);
        return ResponseEntity.ok(response);
    }
}