package com.example.commerce.product.repository;

import com.example.commerce.product.entity.Category;
import com.example.commerce.product.entity.Product;
import com.example.commerce.product.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List; // 추가

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE " +
            "(:keyword IS NULL OR p.name LIKE %:keyword%) AND " +
            "(:category IS NULL OR p.category = :category) AND " +
            "(:status IS NULL OR p.status = :status)")
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("category") Category category,
            @Param("status") ProductStatus status,
            Pageable pageable);

    // --- 대시보드 전용 쿼리 추가 ---

    /**
     * 1. 재고 부족 상품 수 (Summary용)
     * 명세서 기준: 재고가 5개 이하인 상품을 찾습니다.
     */
    long countByStockLessThanEqual(int stockThreshold);

    /**
     * 2. 품절 상품 수 (Widgets용)
     * 예: countByStatus(ProductStatus.SOLD_OUT)
     */
    long countByStatus(ProductStatus status);

    /**
     * 3. 카테고리별 상품 분포 (Charts용)
     * 어떤 카테고리에 상품이 몰려 있는지 비중을 확인합니다.
     */
    @Query("SELECT p.category, COUNT(p) FROM Product p GROUP BY p.category")
    List<Object[]> countByCategoryGroup();
}