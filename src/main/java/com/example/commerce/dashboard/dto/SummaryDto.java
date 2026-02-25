package com.example.commerce.dashboard.dto;

public record SummaryDto(
        long totalAdmins,        // 전체 관리자 수
        long activeAdmins,       // 활성 관리자 수
        long totalCustomers,     // 전체 고객 수
        long activeCustomers,    // 활성 고객 수
        long lowStockProducts,   // 재고 부족(5개 이하) 상품 수
        long todayOrderCount,    // 오늘 들어온 주문 수
        long totalReviewCount,   // [추가] 전체 리뷰 수
        double averageRating     // [추가] 시스템 평균 평점
) {}