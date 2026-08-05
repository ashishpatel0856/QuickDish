package com.ashish.QuickDish.ai.service.impl;

import com.ashish.QuickDish.ai.config.GroqConfig;
import com.ashish.QuickDish.ai.dto.ChatRequestDto;
import com.ashish.QuickDish.ai.dto.ChatResponseDto;
import com.ashish.QuickDish.ai.dto.SmartSearchDto;
import com.ashish.QuickDish.ai.entity.AiConversation;
import com.ashish.QuickDish.ai.repository.AiConversationRepository;
import com.ashish.QuickDish.ai.service.AiService;
import com.ashish.QuickDish.Entity.Restaurant;
import com.ashish.QuickDish.repository.RestaurantRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    private final RestTemplate restTemplate;
    private final GroqConfig groqConfig;
    private final AiConversationRepository conversationRepository;
    private final ObjectMapper objectMapper;
    private final RestaurantRepository restaurantRepository;

    private static final String NON_FOOD_RESPONSE =
            "I'm QuickDish AI, your food ordering assistant!" +
                    "I can help you with" +
                    "• Finding restaurants near you" +
                    "• Ordering delicious food" +
                    "• Tracking your delivery" +
                    "• Food recommendations" +
                    "What would you like to eat today?";

    private static int apiCallCount = 0;
    private static long lastResetTime = System.currentTimeMillis();

 // chat searching
    @Override
    @Transactional
    public ChatResponseDto chat(Long userId, ChatRequestDto request) {
        String sessionId = request.getSessionId() != null ?
                request.getSessionId() : UUID.randomUUID().toString();

        if (!checkRateLimit()) {
            return buildResponse("Rate limit reached. Please try after 1 minute.", sessionId);
        }

        if (!isFoodRelated(request.getMessage())) {
            saveConversation(userId, sessionId, request.getMessage(), NON_FOOD_RESPONSE, 0);
            return buildResponse(NON_FOOD_RESPONSE, sessionId);
        }

        try {
            List<AiConversation> history = conversationRepository
                    .findBySessionIdOrderByCreatedAtDesc(sessionId,
                            org.springframework.data.domain.PageRequest.of(0, 5));

            String response = callGroqApi(request.getMessage(), history);
            saveConversation(userId, sessionId, request.getMessage(), response,
                    estimateTokens(response));

            return buildResponse(response, sessionId);

        } catch (Exception e) {
            log.error("AI error", e);
            return buildResponse("AI service temporarily unavailable.", sessionId);
        }
    }

   // seaching using query
    @Override
    public String interpretSearchQuery(String naturalQuery) {
        if (!checkRateLimit()) return "{}";

        try {
            return extractJson(callGroqApiForSearch(naturalQuery));
        } catch (Exception e) {
            log.error("Search interpretation failed", e);
            return "{}";
        }
    }

    // smart search only quickdish food

    @Override
    public SmartSearchDto smartSearch(String query, Long userId) {
        SmartSearchDto result = new SmartSearchDto();
        result.setQuery(query);

        String interpretedJson = interpretSearchQuery(query);
        boolean aiSuccess = !interpretedJson.equals("{}");

        try {
            JsonNode filters = objectMapper.readTree(interpretedJson);

            List<Restaurant> restaurants;
            if (aiSuccess && filters.has("cuisine") && !filters.get("cuisine").asText().isEmpty()) {
                String cuisine = filters.get("cuisine").asText();
                restaurants = restaurantRepository.findByCategoryContainingIgnoreCase(cuisine);
                result.setInterpretedIntent("AI: " + cuisine + " cuisine");
                result.setFiltersApplied(List.of("Cuisine: " + cuisine));
            } else {
                restaurants = keywordSearch(query);
                result.setInterpretedIntent("Keyword: " + query);
                result.setFiltersApplied(List.of());
            }

            restaurants = filterActiveRestaurants(restaurants);
            result.setResults(mapToResults(restaurants));
            result.setAiExplanation("Found " + restaurants.size() + " restaurants for '" + query + "'");

        } catch (Exception e) {
            log.error("Smart search error", e);
            List<Restaurant> restaurants = keywordSearch(query);
            result.setResults(mapToResults(restaurants));
            result.setInterpretedIntent("Keyword: " + query);
            result.setAiExplanation("Found " + restaurants.size() + " restaurants");
        }

        return result;
    }


    private boolean isFoodRelated(String message) {
        String lower = message.toLowerCase();
        String[] keywords = {
                "food", "eat", "restaurant", "order", "delivery", "biryani", "pizza",
                "burger", "chinese", "indian", "italian", "mexican", "dessert", "beverage",
                "breakfast", "lunch", "dinner", "snack", "meal", "cuisine", "menu",
                "spicy", "sweet", "veg", "non-veg", "vegan", "healthy", "fast food",
                "price", "cheap", "expensive", "discount", "offer", "deal",
                "near me", "nearby", "location", "address", "area",
                "track", "status", "delivery time", "rider", "order status"
        };

        for (String k : keywords) {
            if (lower.contains(k)) return true;
        }
        return false;
    }

    private String callGroqApi(String message, List<AiConversation> history) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqConfig.getApiKey());

        ArrayNode messages = objectMapper.createArrayNode();

        ObjectNode system = objectMapper.createObjectNode();
        system.put("role", "system");
        system.put("content", "You are QuickDish AI, a food ordering assistant. Be concise and helpful.");
        messages.add(system);

        for (AiConversation conv : history) {
            ObjectNode user = objectMapper.createObjectNode();
            user.put("role", "user");
            user.put("content", conv.getUserMessage());
            messages.add(user);

            ObjectNode assistant = objectMapper.createObjectNode();
            assistant.put("role", "assistant");
            assistant.put("content", conv.getAiResponse());
            messages.add(assistant);
        }

        ObjectNode current = objectMapper.createObjectNode();
        current.put("role", "user");
        current.put("content", message);
        messages.add(current);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", groqConfig.getModel());
        body.set("messages", messages);
        body.put("temperature", 0.7);
        body.put("max_tokens", 512);

        HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
        JsonNode response = restTemplate.postForObject(groqConfig.getApiUrl(), entity, JsonNode.class);

        return response.get("choices").get(0).get("message").get("content").asText();
    }

    private String callGroqApiForSearch(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqConfig.getApiKey());

        ArrayNode messages = objectMapper.createArrayNode();

        ObjectNode system = objectMapper.createObjectNode();
        system.put("role", "system");
        system.put("content", "You are a food search parser. Return ONLY JSON: {\"cuisine\":\"...\",\"category\":\"...\",\"maxPrice\":number}");
        messages.add(system);

        ObjectNode user = objectMapper.createObjectNode();
        user.put("role", "user");
        user.put("content", query);
        messages.add(user);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", groqConfig.getModel());
        body.set("messages", messages);
        body.put("temperature", 0.1);
        body.put("max_tokens", 256);

        HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
        JsonNode response = restTemplate.postForObject(groqConfig.getApiUrl(), entity, JsonNode.class);

        return response.get("choices").get(0).get("message").get("content").asText();
    }

    private List<Restaurant> keywordSearch(String query) {
        String q = query.toLowerCase();
        return restaurantRepository.findAll().stream()
                .filter(r -> matchesKeyword(r, q))
                .collect(Collectors.toList());
    }

    private boolean matchesKeyword(Restaurant r, String q) {
        return (r.getName() != null && r.getName().toLowerCase().contains(q)) ||
                (r.getDescription() != null && r.getDescription().toLowerCase().contains(q)) ||
                (r.getCategory() != null && r.getCategory().toLowerCase().contains(q));
    }

    private List<Restaurant> filterActiveRestaurants(List<Restaurant> restaurants) {
        return restaurants.stream()
                .filter(r -> r.getIsActive() != null && r.getIsActive())
                .filter(r -> r.isApproved())
                .limit(20)
                .collect(Collectors.toList());
    }

    private List<SmartSearchDto.SearchResultDto> mapToResults(List<Restaurant> restaurants) {
        List<SmartSearchDto.SearchResultDto> results = new ArrayList<>();
        for (Restaurant r : restaurants) {
            SmartSearchDto.SearchResultDto dto = new SmartSearchDto.SearchResultDto();
            dto.setId(r.getId());
            dto.setName(r.getName());
            dto.setType("RESTAURANT");
            dto.setRelevanceScore(r.getRating() != null ? r.getRating() : 0.0);
            results.add(dto);
        }
        return results;
    }

    private synchronized boolean checkRateLimit() {
        long now = System.currentTimeMillis();
        if (now - lastResetTime > 60000) {
            apiCallCount = 0;
            lastResetTime = now;
        }
        if (apiCallCount >= 20) return false;
        apiCallCount++;
        return true;
    }

    private ChatResponseDto buildResponse(String msg, String sessionId) {
        return ChatResponseDto.builder()
                .response(msg)
                .sessionId(sessionId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private void saveConversation(Long userId, String sessionId, String msg, String resp, int tokens) {
        try {
            conversationRepository.save(AiConversation.builder()
                    .userId(userId)
                    .sessionId(sessionId)
                    .featureType("CHATBOT")
                    .userMessage(msg)
                    .aiResponse(resp)
                    .tokensUsed(tokens)
                    .build());
        } catch (Exception e) {
            log.error("Save failed", e);
        }
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start != -1 && end > start) return text.substring(start, end + 1);
        return "{}";
    }

    private int estimateTokens(String text) {
        return text.length() / 4;
    }
}