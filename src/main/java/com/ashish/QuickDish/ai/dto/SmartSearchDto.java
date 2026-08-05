package com.ashish.QuickDish.ai.dto;

import lombok.Data;

import java.util.List;

@Data
public class SmartSearchDto {
    private String query;
    private String interpretedIntent;
    private List<SearchResultDto> results;
    private List<String> filtersApplied;
    private String aiExplanation;

    @Data
    public static class SearchResultDto {
        private Long id;
        private String name;
        private String type;
        private Double relevanceScore;
    }
}