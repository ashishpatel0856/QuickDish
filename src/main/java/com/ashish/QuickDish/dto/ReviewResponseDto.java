package com.ashish.QuickDish.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponseDto {

    private Long id;
    private String name;
    private String comments;
    private int rating;
    private LocalDateTime date;
    private Long userId;
    private Long restaurantId;

}
