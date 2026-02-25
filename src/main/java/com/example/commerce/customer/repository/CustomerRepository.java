package com.example.commerce.customer.repository;

import com.example.commerce.customer.entity.Customer;
import com.example.commerce.customer.entity.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List; // [추가] 차트 데이터를 위해 필요해요
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // 이메일 중복체크
    boolean existsByEmail(String email);

    // 등록된 이메일 확인
    Optional<Customer> findByEmail(String email);

    @Query("SELECT c FROM Customer c WHERE " +
            "(:keyword IS NULL OR c.name LIKE %:keyword% OR c.email LIKE %:keyword%) AND " +
            "(:status IS NULL OR c.status = :status)")
    Page<Customer> searchCustomers(
            @Param("keyword") String keyword,
            @Param("status") CustomerStatus status,
            Pageable pageable);

    // 대시보드 전용 쿼리 추가

    /**
     * 1. 특정 상태의 고객 수 카운트 (Summary용)
     * Spring Data JPA의 메서드 명명 규칙만으로 쿼리가 생성됩니다.
     */
    long countByStatus(CustomerStatus status);

    /**
     * 2. 고객 상태별 그룹 카운트 (Chart용)
     * Active, Inactive, Suspended 각각의 개수를 한 번에 가져옵니다.
     * 결과는 [상태, 개수] 형태의 리스트로 반환됩니다.
     */
    @Query("SELECT c.status, COUNT(c) FROM Customer c GROUP BY c.status")
    List<Object[]> countByStatusGroup();
}