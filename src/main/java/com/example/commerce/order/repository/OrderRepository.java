package com.example.commerce.order.repository;

import com.example.commerce.customer.entity.Customer;
import com.example.commerce.order.entity.Order;
import com.example.commerce.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime; // [추가] 날짜 계산을 위해 필요해요
import java.util.List;          // [추가]

public interface OrderRepository extends JpaRepository<Order, Long>{
    @Query("SELECT o FROM Order o WHERE " +
            "(:customer IS NULL OR :customer = o.customer) AND " +
            "(:keyword IS NULL OR o.customer.name LIKE %:keyword% OR CAST(o.orderNo AS string) LIKE %:keyword%) AND " +
            "(:status IS NULL OR o.orderStatus = :status)")
    Page<Order> searchOrders(
            @Param("keyword") String keyword,
            @Param("status") OrderStatus status,
            @Param("customer") Customer customer,
            Pageable pageable);

    // 대시보드 전용 쿼리

    /**
     * 1. 오늘 들어온 주문 수 (Summary용)
     */
    long countByCreatedAtAfter(LocalDateTime startOfDay);

    /**
     * 2. 누적 총 매출 금액 (Widgets용)
     * COALESCE를 사용해서 주문이 하나도 없을 때 null 대신 0이 나오게 방어합니다.
     */
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o")
    long sumTotalRevenue();

    /**
     * 3. 오늘 발생한 매출 금액 (Widgets용)
     */
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.createdAt >= :startOfDay")
    long sumTodayRevenue(@Param("startOfDay") LocalDateTime startOfDay);

    /**
     * 4. 주문 상태별 주문 수 (Widgets용)
     * Preparing, Shipping 등을 그룹화해서 한 번에 가져옵니다.
     */
    @Query("SELECT o.orderStatus, COUNT(o) FROM Order o GROUP BY o.orderStatus")
    List<Object[]> countByOrderStatusGroup();
}