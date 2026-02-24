package com.example.commerce.review.dto;

import lombok.Getter;

@Getter
public class CreateReviewRequest {
    private String content;
    private int rating;
}
