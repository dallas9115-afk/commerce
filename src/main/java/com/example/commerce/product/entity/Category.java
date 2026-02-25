package com.example.commerce.product.entity;

import com.example.commerce.global.exception.ErrorCode;
import com.example.commerce.global.exception.ServiceException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    VEGETABLE_FRUIT("농산물"),
    MEAT_EGG("축산물"),
    SEAFOOD("수산물"),
    MEAL_KIT("밀키트/간편식"),
    SIDE_DISH("밑반찬/김치"),
    DAIRY_DELI("유제품/델리"),
    BEVERAGE("생수/음료"),
    PANTRY("면/양념/오일"),
    SNACK_BAKERY("간식/베이커리"),
    HEALTH_FOOD("건강식품");

    private final String categoryName;

    public static Category from(String role) {
        if (role == null || role.isBlank()) {
            throw new ServiceException(ErrorCode.INVALID_CATEGORY);
        }

        try {
            // Enum 상수를 즉시 찾아 반환하며, 없을 경우 예외를 잡아 커스텀 예외를 던집니다.
            return Category.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new ServiceException(ErrorCode.INVALID_CATEGORY);
        }
    }
}