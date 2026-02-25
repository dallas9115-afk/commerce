package com.example.commerce.product.dto;

import com.example.commerce.review.dto.GetOneReviewResponse;
import com.example.commerce.review.dto.ReviewRating;

import java.time.LocalDateTime;
import java.util.List;

public record GetOneProductResponse(
        String productName,
        String categoryName,
        int productPrice,
        int productStock,
        String statusName,
        LocalDateTime createdAt,
        String adminName,
        String adminEmail,

        List<GetOneReviewResponse> top3ReviewOfProduct,
        int countOfReview,
        double averageOfReview,
        List<ReviewRating> reviewRatingList
) {}
