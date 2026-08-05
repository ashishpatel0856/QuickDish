package com.ashish.QuickDish.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequestDto {

    private String name;
    private String comments;
    private int rating;
    private Long userId;
    private Long restaurantId;
}
