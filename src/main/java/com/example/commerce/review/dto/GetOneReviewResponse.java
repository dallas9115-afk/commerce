package com.example.commerce.review.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record GetOneReviewResponse(
        Long reviewId,
        UUID orderNo,
        String customerName,
        String customerEmail,
        String productName,
        int rating,
        String content,
        LocalDateTime createAt
){}
