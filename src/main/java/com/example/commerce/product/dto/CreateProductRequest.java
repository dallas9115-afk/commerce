package com.example.commerce.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;

@Getter
public class CreateProductRequest {
    @NotBlank(message = "상품명은 필수입니다.")
    private String productName;

    @NotBlank(message = "카테고리 설정은 필수입니다.")
    private String category;

    @NotNull(message = "가격은 필수입니다.")
    @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.") //  [수정] 빈 문자열 메시지 채움
    private Integer productPrice;

    @NotNull(message = "재고는 필수입니다.")
    @Min(value = 0, message = "재고는 0개 이상이어야 합니다.") //  [수정] 메시지 추가
    private Integer productStock;

    private String productStatus;
}