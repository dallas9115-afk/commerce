package com.example.commerce.product.entity;

import com.example.commerce.global.exception.ErrorCode;
import com.example.commerce.global.exception.ServiceException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {
    AVAILABLE("판매중"),
    SOLD_OUT("품절"),
    DISCONTINUED("단종");

    private final String statusName;

    public static ProductStatus from(String status) {
        if (status == null || status.isBlank()) {
            throw new ServiceException(ErrorCode.INVALID_STATUS);
        }

        try {
            return ProductStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new ServiceException(ErrorCode.INVALID_STATUS);
        }
    }
}