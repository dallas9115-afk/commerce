package com.example.commerce.dashboard.controller;

import com.example.commerce.dashboard.dto.DashboardResponse;
import com.example.commerce.dashboard.service.DashboardService;
import com.example.commerce.global.common.CommonResponseDTO;
import com.example.commerce.global.common.CommonResponseHandler;
import com.example.commerce.global.common.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admins/dashboard") // 관리자 전용 경로 설정
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 대시보드 전체 데이터 조회
     * 명세서 요구사항: Summary, Widgets, Charts, 최근 주문 목록을 한 번에 반환
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OP_ADMIN', 'CS_ADMIN')") // 모든 관리자가 볼 수 있도록 설정
    public ResponseEntity<CommonResponseDTO<DashboardResponse>> getDashboard() {

        // 1. 서비스에서 가공된 대시보드 통합 데이터 가져오기
        DashboardResponse response = dashboardService.getDashboardData();

        // 2. 팀 컨벤션에 맞춘 표준 응답 핸들러로 반환
        // SuccessCode.GET_SUCCESSFUL ("데이터 조회에 성공했습니다.") 사용
        return CommonResponseHandler.success(SuccessCode.GET_SUCCESSFUL, response);
    }
}