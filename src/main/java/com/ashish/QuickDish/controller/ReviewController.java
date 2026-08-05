package com.ashish.QuickDish.controller;

import com.ashish.QuickDish.dto.ReviewRequestDto;
import com.ashish.QuickDish.dto.ReviewResponseDto;
import com.ashish.QuickDish.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/review")
public class ReviewController {
   private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponseDto> addToView(@RequestBody ReviewRequestDto reviewRequestDto) {
        ReviewResponseDto reviewResponseDto = reviewService.addToView(reviewRequestDto);
        return new ResponseEntity<>(reviewResponseDto, HttpStatus.CREATED);
    }

    @GetMapping("{reviewId}")
    public ResponseEntity<ReviewResponseDto> getReviewById(@PathVariable Long reviewId) {
        ReviewResponseDto reviewResponseDto = reviewService.getReviewById(reviewId);
        return new ResponseEntity<>(reviewResponseDto, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponseDto>> getAllReviews() {
        List<ReviewResponseDto> reviewResponseDto = reviewService.getAllReviews();
        return new ResponseEntity<>(reviewResponseDto, HttpStatus.OK);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReviewById(@PathVariable Long reviewId, @RequestBody ReviewRequestDto reviewRequestDto) {
       ReviewResponseDto reviewResponseDto = reviewService.updateReviewById(reviewId, reviewRequestDto);
       return new ResponseEntity<>(reviewResponseDto,HttpStatus.OK);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDto> deleteReviewById(@PathVariable Long reviewId) {
        reviewService.deleteReviewById(reviewId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
