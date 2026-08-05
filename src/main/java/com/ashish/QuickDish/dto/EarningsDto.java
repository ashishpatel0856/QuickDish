package com.ashish.QuickDish.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EarningsDto {
    private Double totalEarnings;
    private Integer totalDeliveries;
    private String from;
    private String to;
}