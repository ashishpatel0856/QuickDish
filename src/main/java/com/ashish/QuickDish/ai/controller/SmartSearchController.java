package com.ashish.QuickDish.ai.controller;

import com.ashish.QuickDish.ai.dto.SmartSearchDto;
import com.ashish.QuickDish.ai.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai/search")
@RequiredArgsConstructor
@Tag(name = "AI Smart Search", description = "Natural language food search")
public class SmartSearchController {

    private final AiService aiService;

    @GetMapping("/smart")
    @Operation(summary = "AI-powered food search")
    public ResponseEntity<SmartSearchDto> smartSearch(
            @RequestParam String query,
            @RequestParam(required = false) Long userId) {

        SmartSearchDto results = aiService.smartSearch(query, userId);
        return ResponseEntity.ok(results);
    }
}