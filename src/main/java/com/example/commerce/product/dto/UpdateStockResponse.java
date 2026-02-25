package com.example.commerce.product.dto;

import java.time.LocalDateTime;

public record UpdateStockResponse(
        Long productId,
        String productName,
        int previousStock,
        int newStock,
        LocalDateTime modifiedAt
) {}