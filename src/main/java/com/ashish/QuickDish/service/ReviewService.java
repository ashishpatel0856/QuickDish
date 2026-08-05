package com.ashish.QuickDish.service;

import com.ashish.QuickDish.dto.ReviewRequestDto;
import com.ashish.QuickDish.dto.ReviewResponseDto;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    ReviewResponseDto addToView(ReviewRequestDto reviewRequestDto);
    ReviewResponseDto getReviewById(Long id);
    List<ReviewResponseDto> getAllReviews();
    ReviewResponseDto updateReviewById(Long id,ReviewRequestDto reviewRequestDto);
    void deleteReviewById(Long id);

}
