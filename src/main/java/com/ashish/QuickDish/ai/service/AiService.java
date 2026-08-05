package com.ashish.QuickDish.ai.service;

import com.ashish.QuickDish.ai.dto.ChatRequestDto;
import com.ashish.QuickDish.ai.dto.ChatResponseDto;
import com.ashish.QuickDish.ai.dto.SmartSearchDto;

public interface AiService {
    ChatResponseDto chat(Long userId, ChatRequestDto request);
    String interpretSearchQuery(String naturalQuery);
    SmartSearchDto smartSearch(String query, Long userId);
}