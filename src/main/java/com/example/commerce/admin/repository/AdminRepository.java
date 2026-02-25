package com.example.commerce.admin.repository;

import com.example.commerce.admin.entity.Admin;
import com.example.commerce.admin.entity.AdminStatus;
import com.example.commerce.admin.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    boolean existsByEmail(String email);
    Optional<Admin> findByEmail(String email);

    @Query("SELECT a FROM Admin a WHERE " +
            "(:keyword IS NULL OR a.name LIKE %:keyword% OR a.email LIKE %:keyword%) AND " +
            "(:role IS NULL OR a.role = :role) AND " +
            "(:status IS NULL OR a.status = :status)")
    Page<Admin> searchAdmins(
            @Param("keyword") String keyword,
            @Param("role") Role role,
            @Param("status") AdminStatus status,
            Pageable pageable);

    // 대시보드 전용 쿼리 추가

    /**
     * 1. 특정 상태의 관리자 수 카운트 (Summary용)
     * 예: countByStatus(AdminStatus.ACTIVE) -> 활성 관리자 수
     */
    long countByStatus(AdminStatus status);
}