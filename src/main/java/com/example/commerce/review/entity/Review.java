package com.example.commerce.review.entity;

import com.example.commerce.customer.entity.Customer;
import com.example.commerce.global.common.BaseEntity;
import com.example.commerce.order.entity.Order;
import com.example.commerce.product.entity.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

@Getter
@Entity
@Table(name="reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column
    private int rating;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    public Review(String content, int rating, Order order) {
        this.content = content;
        this.rating = rating;
        this.order = order;
    }
}
