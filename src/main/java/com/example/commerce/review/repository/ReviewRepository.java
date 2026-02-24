package com.example.commerce.review.repository;

import com.example.commerce.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE" +
            "(:productId = r.order.product.id) AND " +
            "(:keyword IS NULL OR r.order.customer.name LIKE %:keyword% OR r.order.product.name LIKE %:keyword%) AND " +
            "(:rating IS NULL OR r.rating = :rating)")
    Page<Review> searchReviews(
            @Param("productId") Long productId,
            @Param("keyword") String keyword,
            @Param("rating") int rating,
            PageRequest pageable);
}
