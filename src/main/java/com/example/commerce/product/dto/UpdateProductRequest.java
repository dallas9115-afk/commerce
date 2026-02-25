package com.example.commerce.product.dto;

import com.example.commerce.product.entity.Category;
import com.example.commerce.product.entity.ProductStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;

@Getter
public class UpdateProductRequest {
    @NotBlank(message = "상품명은 필수입니다.")
    private String productName;

    @NotBlank(message = "카테고리 설정은 필수입니다.")
    private String category;
    //private Category category;

    @NotNull(message = "가격은 필수입니다.")
    @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.") // 추가
    private Integer productPrice;
}
