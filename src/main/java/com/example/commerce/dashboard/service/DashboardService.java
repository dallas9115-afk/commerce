package com.example.commerce.dashboard.service;

import com.example.commerce.admin.entity.AdminStatus;
import com.example.commerce.admin.repository.AdminRepository;
import com.example.commerce.customer.entity.CustomerStatus;
import com.example.commerce.customer.repository.CustomerRepository;
import com.example.commerce.dashboard.dto.*;
import com.example.commerce.order.dto.GetOrdersByAdminResponse;
import com.example.commerce.order.entity.Order;
import com.example.commerce.order.repository.OrderRepository;
import com.example.commerce.product.entity.ProductStatus;
import com.example.commerce.product.repository.ProductRepository;
import com.example.commerce.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 대시보드는 조회 전용이므로 성능을 위해 readOnly 설정
public class DashboardService {

    private final AdminRepository adminRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    public DashboardResponse getDashboardData() {
        // 0. 기준 시간 설정 (오늘 00:00:00)
        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        // 💡 [수정 완료] roundedAvg 빨간줄 해결! DB에서 평균값을 가져와서 소수점 1자리로 반올림합니다.
        Double avgRating = reviewRepository.averageGlobalRating();
        double roundedAvg = (avgRating != null) ? Math.round(avgRating * 10) / 10.0 : 0.0;

        // 1. Summary 데이터 집계
        SummaryDto summary = new SummaryDto(
                adminRepository.count(),
                adminRepository.countByStatus(AdminStatus.ACTIVE),
                customerRepository.count(),
                customerRepository.countByStatus(CustomerStatus.ACTIVE),
                productRepository.countByStockLessThanEqual(5), // 명세서 조건: 5개 이하
                orderRepository.countByCreatedAtAfter(startOfToday),
                reviewRepository.countAllReviews(), // [추가]
                roundedAvg                          // [추가]
        );

        // 2. Widgets 데이터 집계
        WidgetDto widgets = new WidgetDto(
                orderRepository.sumTotalRevenue(),
                orderRepository.sumTodayRevenue(startOfToday),
                productRepository.countByStatus(ProductStatus.SOLD_OUT),
                convertListToMap(orderRepository.countByOrderStatusGroup()) // 그룹화 쿼리 결과를 Map으로 변환
        );

        // 3. Charts 데이터 집계
        ChartDto charts = new ChartDto(
                reviewRepository.countAllReviewRatings().stream()
                        .collect(Collectors.toMap(r -> r.getRating(), r -> (long) r.getCount())),
                convertListToMap(customerRepository.countByStatusGroup()),
                convertListToMap(productRepository.countByCategoryGroup())
        );

        // 4. 최근 주문 목록 10개 (내림차순 정렬)
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> recentOrdersPage = orderRepository.findAll(pageRequest);

        List<GetOrdersByAdminResponse> recentOrders = recentOrdersPage.getContent().stream()
                .map(order -> new GetOrdersByAdminResponse(
                        order.getId(),
                        order.getOrderNo(),
                        order.getCustomer().getName(),
                        order.getProduct().getName(),
                        order.getOrderStatus().getStatusName(),
                        order.getQuantity(),
                        order.getCreatedAt(),
                        order.getAdmin() != null ? order.getAdmin().getName() : "N/A"
                )).toList();

        return new DashboardResponse(summary, widgets, charts, recentOrders);
    }

    /**
     * 리포지토리의 List<Object[]> (GROUP BY 결과)를 Map<String, Long>으로 변환해주는 헬퍼 메서드
     * 예: [ACTIVE, 10] -> {"ACTIVE": 10}
     */
    private Map<String, Long> convertListToMap(List<Object[]> list) {
        return list.stream()
                .collect(Collectors.toMap(
                        obj -> obj[0].toString(), // Enum 또는 객체의 이름을 Key로
                        obj -> (Long) obj[1]      // COUNT 결과를 Value로
                ));
    }
}