package com.example.commerce.order.controller;

import com.example.commerce.global.common.CommonResponseDTO;
import com.example.commerce.global.common.CommonResponseHandler;
import com.example.commerce.global.common.SuccessCode;
import com.example.commerce.global.security.UserPrincipal;
import com.example.commerce.order.dto.*;
import com.example.commerce.order.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import com.example.commerce.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class OrderController {

    private final OrderService orderService;

    // 1. 고객의 주문 생성
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/orders")
    ResponseEntity<CommonResponseDTO<CreateOrderResponse>> create(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        CreateOrderResponse response = orderService.create(userPrincipal, request);
        return CommonResponseHandler.success(SuccessCode.ORDER_SUCCESSFUL, response);
    }

    // 2. 관리자의 주문 생성
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OP_ADMIN', 'CS_ADMIN')")
    @PostMapping("/admins/orders")
    ResponseEntity<CommonResponseDTO<CreateOrderByAdminResponse>> create(
            @Valid @RequestBody CreateOrderByAdminRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        CreateOrderByAdminResponse response = orderService.createByAdmin(userPrincipal, request);
        return CommonResponseHandler.success(SuccessCode.ORDER_SUCCESSFUL, response);
    }

    // 3. 관리자의 주문 전체 조회 [수정] /admin/orders -> /admins/orders 로 통일
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OP_ADMIN', 'CS_ADMIN')")
    @GetMapping("/admins/orders")
    public ResponseEntity<CommonResponseDTO<List<GetOrdersByAdminResponse>>> getAllByAdmin(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Page<GetOrdersByAdminResponse> response = orderService.getAllByAdmin(keyword, status, pageable, userPrincipal);
        return CommonResponseHandler.success(SuccessCode.ORDER_SUCCESSFUL, response.getContent());
    }

    // 4. 고객의 주문 전체 조회
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/orders")
    public ResponseEntity<CommonResponseDTO<List<GetOrdersResponse>>> getAllbyCustomer(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Page<GetOrdersResponse> response = orderService.getAllByCustomer(userPrincipal, keyword, status, pageable);
        return CommonResponseHandler.success(SuccessCode.ORDER_SUCCESSFUL, response.getContent());
    }

    // 5. 관리자의 주문 단건 조회
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OP_ADMIN', 'CS_ADMIN')")
    @GetMapping("/admins/orders/{id}")
    ResponseEntity<CommonResponseDTO<GetOneOrderByAdminResponse>> getOne(
            @PathVariable("id") Long orderId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        GetOneOrderByAdminResponse response = orderService.getOneAdminOrder(orderId, userPrincipal);
        return CommonResponseHandler.success(SuccessCode.GET_SUCCESSFUL, response);
    }

    // 6. 고객의 주문 단건 조회
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/orders/{id}")
    ResponseEntity<CommonResponseDTO<GetOneOrderResponse>> getOneOrder(
            @PathVariable("id") Long orderId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        GetOneOrderResponse response = orderService.getOneOrder(orderId, userPrincipal);
        return CommonResponseHandler.success(SuccessCode.GET_SUCCESSFUL, response);
    }

    // 7. 관리자의 주문 취소
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OP_ADMIN', 'CS_ADMIN')")
    @PatchMapping("/admins/orders/{id}/cancel")
    ResponseEntity<CommonResponseDTO<CancelOrderResponse>> cancelByAdmin(
            @PathVariable("id") Long orderId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CancelOrderRequest request) {
        CancelOrderResponse response = orderService.cancelByAdmin(orderId, userPrincipal, request);
        return CommonResponseHandler.success(SuccessCode.DELETE_SUCCESSFUL, response);
    }

    // 8. [추가] 고객 본인의 주문 취소 (기존 서비스 로직 재사용)
    @PreAuthorize("hasRole('CUSTOMER')")
    @PatchMapping("/orders/{id}/cancel")
    ResponseEntity<CommonResponseDTO<CancelOrderResponse>> cancelByCustomer(
            @PathVariable("id") Long orderId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CancelOrderRequest request) {
        CancelOrderResponse response = orderService.cancelByAdmin(orderId, userPrincipal, request);
        return CommonResponseHandler.success(SuccessCode.DELETE_SUCCESSFUL, response);
    }

    // 9. 주문 완료 (배송 완료)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OP_ADMIN', 'CS_ADMIN')")
    @PatchMapping("/admins/orders/{id}/delivered")
    ResponseEntity<CommonResponseDTO<String>> deliverCompleted(
            @PathVariable("id") Long orderId,
            @AuthenticationPrincipal UserPrincipal userPrincipal){
        orderService.deliverCompleted(orderId, userPrincipal);
        return CommonResponseHandler.success(SuccessCode.DATA_UPDATED, "배달이 완료되었습니다.");
    }
}