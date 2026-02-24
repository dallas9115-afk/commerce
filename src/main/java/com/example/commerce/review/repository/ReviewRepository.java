package com.example.commerce.review.repository;

import com.example.commerce.review.dto.GetOneReviewResponse;
import com.example.commerce.review.dto.ReviewRating;
import com.example.commerce.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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

    @Query("SELECT r FROM Review r WHERE" +
            "(:productId = r.order.product.id) AND "+
            "(LIMIT 3)"
    )
    List<GetOneReviewResponse> searchReviewsByProductId(
            @Param("productId") Long productId,
            Pageable pageable);

    // 상품 리뷰 개수
    @Query("SELECT COUNT(r) FROM Review r WHERE r.order.product.id = :productId")
    int countByProductId(@Param("productId") Long productId);

    // 상품 리뷰 평균
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.order.product.id = :productId")
    Double averageRating(@Param("productId") Long productId);

    // 상품 별점 별 리뷰 개수
    @Query("SELECT new com.example.commerce.review.dto.ReviewRating(r.rating, COUNT(r)) FROM Review r " +
            "WHERE :productId = r.order.product.id" +
            "GROUP BY r.rating" +
            "ORDER BY r.rating")
    List<ReviewRating> countListByProductId(@Param("productId") Long productId);
}
