package com.example.commerce.dashboard.dto;

import java.util.Map;

public record ChartDto(
        Map<Integer, Long> reviewRatingDistribution, // 별점(1~5)별 개수
        Map<String, Long> customerStatusDistribution, // 고객 상태별 개수
        Map<String, Long> categoryDistribution       // 카테고리별 상품 개수
) {}