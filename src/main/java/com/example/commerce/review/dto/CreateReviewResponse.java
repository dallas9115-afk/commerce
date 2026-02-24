package com.example.commerce.review.dto;

import java.time.LocalDateTime;

public record CreateReviewResponse(
        Long reviewId,
        String productName,
        String customerName,
        LocalDateTime orderAt,
        int rating,
        String content
){}