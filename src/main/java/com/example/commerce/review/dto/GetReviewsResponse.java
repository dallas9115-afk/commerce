package com.example.commerce.review.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record GetReviewsResponse(
        Long reviewId,
        UUID orderNo,
        String customerName,
        String productName,
        int rating,
        String content,
        LocalDateTime createdAt
) {
}
