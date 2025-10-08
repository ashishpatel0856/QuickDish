package com.ashish.QuickDish.service;

import com.ashish.QuickDish.Entity.Restaurant;
import com.ashish.QuickDish.Entity.Review;
import com.ashish.QuickDish.Entity.User;
import com.ashish.QuickDish.dto.ReviewRequestDto;
import com.ashish.QuickDish.dto.ReviewResponseDto;
import com.ashish.QuickDish.exceptions.ResourceNotFoundException;
import com.ashish.QuickDish.exceptions.UnAuthorisedException;
import com.ashish.QuickDish.repository.RestaurantRepository;
import com.ashish.QuickDish.repository.ReviewRepository;
import com.ashish.QuickDish.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.ashish.QuickDish.utils.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final RestaurantRepository restaurantRepository;
    private final UserService userService;



    @Override
    public ReviewResponseDto addToView(ReviewRequestDto reviewRequestDto) {
        log.info("add review restaurant by users");
        Review review = new Review();
        review.setName(reviewRequestDto.getName());
        review.setRating(reviewRequestDto.getRating());
        review.setComments(reviewRequestDto.getComments());
        review.setDate(LocalDateTime.now());

        User user = userRepository.findById(reviewRequestDto.getUserId()).orElseThrow(
                () -> new ResourceNotFoundException("USER NOT FOUND"));

        Restaurant restaurant = restaurantRepository.findById(reviewRequestDto.getRestaurantId()).orElseThrow(
                () -> new ResourceNotFoundException(" restaurant not found"));

        review.setRestaurant(restaurant);
        review.setUser(user);
        Review savedReview = reviewRepository.save(review);
        return modelMapper.map(savedReview, ReviewResponseDto.class);
    }

    @Override
    public ReviewResponseDto getReviewById(Long id) {
        log.info("getting  review by id");

        Review review = reviewRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("REVIEW NOT FOUND with review Id"));

        User user = getCurrentUser();
//        if ( !review.getId().equals(user.getId())) {
//            throw new UnAuthorisedException("YOU ARE NOT UNAUTHORIZED PERSON");
//        }
        return modelMapper.map(review, ReviewResponseDto.class);
    }

    @Override
    public List<ReviewResponseDto> getAllReviews() {
        log.info("getting  all reviews");
        List<Review> reviews = reviewRepository.findAll();
        return  reviews
                .stream()
                .map(review -> modelMapper.map(review, ReviewResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public ReviewResponseDto updateReviewById(Long id, ReviewRequestDto reviewRequestDto) {
        log.info("updating review by users");
        Review review = reviewRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(" not updated because review id not found"));


        review.setName(reviewRequestDto.getName());
        review.setRating(reviewRequestDto.getRating());
        review.setComments(reviewRequestDto.getComments());
        review.setDate(LocalDateTime.now());
        Review savedReview = reviewRepository.save(review);
        return modelMapper.map(savedReview, ReviewResponseDto.class);
    }

    @Override
    public void deleteReviewById(Long id) {
        log.info("deleting review by users");
        Review review = reviewRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("review not found"));
        reviewRepository.delete(review);

    }
}
