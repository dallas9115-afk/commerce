package com.example.commerce.dashboard.dto;

import java.util.Map;

public record WidgetDto(
        long totalRevenue,       // 총 누적 매출
        long todayRevenue,       // 오늘 매출
        long soldOutProducts,    // 품절 상품 수
        Map<String, Long> orderStatusCount // 주문 상태별 수량 (PREPARING: 5, DELIVERED: 10 등)
) {}