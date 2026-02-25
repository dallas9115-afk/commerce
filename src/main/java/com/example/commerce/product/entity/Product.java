package com.example.commerce.product.entity;

import com.example.commerce.admin.entity.Admin;
import com.example.commerce.global.common.BaseEntity;
import com.example.commerce.global.exception.ErrorCode;
import com.example.commerce.global.exception.ServiceException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 상품 고유 id

    @Column(nullable = false, length = 50)
    private String name; // 상품 이름

    @Column(nullable = false, length = 100)
    @Enumerated(EnumType.STRING) // [참고] Category가 Enum이라면 추가 확인 필요
    private Category category; // 상품 설명

    @Column(nullable = false)
    private int price; // 상품 가격

    @Column(nullable = false)
    private int stock; // 재고 수량

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin admin; //cs 관리자가 주문을 생성했을 때 저장됨 -> nullable

    // 상품 생성
    public Product (String name, Category category, int price, int stock, ProductStatus status, Admin admin){
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.status = status;
        this.admin = admin;
    }

    // 상품 수정
    // [수정] int price -> Integer price 로 변경
    public void update (String name, Category category, Integer price) {
        this.name = name;
        this.category = category;

        // [추가] 가격이 null이 아닐 때만 유효성 검사 및 업데이트 진행
        if (price != null) {
            if (priceIsValid(price)) {
                this.price = price;
            }
        }
    }

    public boolean priceIsValid(int price){
        if (price<0) throw new ServiceException(ErrorCode.MINUS_PRICE);
        return true;
    }

    public boolean stockIsValid(int stock){
        if (stock<0) throw new ServiceException(ErrorCode.SHORT_STOCK);
        return true;
    }

    public void chkStock(int quantity){
        if (quantity > this.stock){
            throw new ServiceException(ErrorCode.SHORT_STOCK);
        }
    }

    // 재고처리
    public void updateStock(int stock){
        // 계산된 값이 들어오도록 함
        if(stockIsValid(stock)){
            this.stock = stock;
        }

        // [수정 완료] 단종(DISCONTINUED) 상태가 아닐 때만 자동 상태 전환 처리
        if (this.status != ProductStatus.DISCONTINUED) {
            if (this.stock <= 0) {
                updateStatus(ProductStatus.SOLD_OUT); // 재고가 0 이하이면 품절
            } else {
                updateStatus(ProductStatus.AVAILABLE); // 재고가 1 이상이면 다시 판매중
            }
        }
    }

    // 상태 변경
    public void updateStatus(ProductStatus productStatus) {
        this.status = productStatus;
    }
}