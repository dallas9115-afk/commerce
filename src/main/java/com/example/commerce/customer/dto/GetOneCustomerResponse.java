package com.example.commerce.customer.dto;

import java.time.LocalDateTime;

public record GetOneCustomerResponse(
        Long customerId,
        String customerName,
        String customerEmail,
        String customerPhone,
        String customerStatus,
        LocalDateTime customerCreatedAt,
        LocalDateTime customerModifiedAt,
        long totalOrderCount,     // [추가] 총 주문 수
        long totalPurchaseAmount  // [추가] 총 구매 금액
) {}
