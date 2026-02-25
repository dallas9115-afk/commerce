package com.example.commerce.order.entity;

import com.example.commerce.admin.entity.Admin;
import com.example.commerce.customer.entity.Customer;
import com.example.commerce.global.common.BaseEntity;
import com.example.commerce.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID orderNo = UUID.randomUUID();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private int quantity;

    @Column
    private String cancelReason;

    private boolean isReviewed;

    private long totalPrice;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin admin;

    // 생성자 수정: orderStatus 초기화 로직 추가
    public Order(int quantity, long totalPrice, Product product, Customer customer, Admin admin, boolean isReviewed) {
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.product = product;
        this.customer = customer;
        this.admin = admin;
        this.isReviewed = isReviewed;
        this.orderStatus = OrderStatus.PREPARING; // 신규 주문 시 기본 상태 설정
    }

    public void cancel(String reason) {
        if (orderStatus != OrderStatus.PREPARING){
            throw new com.example.commerce.global.exception.ServiceException(com.example.commerce.global.exception.ErrorCode.INVALID_STATUS);
        }
        updateStatus(OrderStatus.CANCELED);
        this.cancelReason = reason;
    }

    public void updateStatus(OrderStatus orderStatus){
        this.orderStatus = orderStatus;
    }

    public void updateIsReviewd(){
        this.isReviewed = true; // 리뷰 작성 시 true로 변경하는 로직
    }
}