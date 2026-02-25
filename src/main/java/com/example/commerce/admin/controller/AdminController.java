package com.example.commerce.admin.controller;

import com.example.commerce.admin.dto.*;
import com.example.commerce.admin.entity.AdminStatus;
import com.example.commerce.admin.entity.Role;
import com.example.commerce.admin.service.AdminService;
import com.example.commerce.global.common.CommonResponseDTO;
import com.example.commerce.global.common.CommonResponseHandler;
import com.example.commerce.global.common.SuccessCode;
import com.example.commerce.global.security.AdminUserDetails;
import com.example.commerce.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admins")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/signUp")
    ResponseEntity<CommonResponseDTO<SignupAdminResponse>> signup(
            @Valid @RequestBody SignupAdminRequest request
    ){
        SignupAdminResponse response = adminService.signup(request);
        return CommonResponseHandler.success(SuccessCode.ADMIN_SIGNUP, response);
    }

    // 세션 잔재 없음, 순수 JWT 발급 로직
    @PostMapping("/logIn")
    public ResponseEntity<CommonResponseDTO<LoginAdminResponse>> login(
            @Valid @RequestBody LoginAdminRequest request
    ){
        LoginAdminResponse response = adminService.login(request);
        return CommonResponseHandler.success(SuccessCode.LOGIN_SUCCESSFUL, response);
    }

    // 로그아웃 API 검증 (DB의 리프레시 토큰 삭제) (액세스 토큰은 클라이언트에게 발급되므로.)
    @PostMapping("/logOut")
    public ResponseEntity<CommonResponseDTO<Void>> logout(
            @AuthenticationPrincipal AdminUserDetails userDetails
    ) {
        adminService.logout(userDetails.getAdmin().getId());
        return CommonResponseHandler.success(SuccessCode.DATA_UPDATED);
    }

    // 관리자 리스트 조회 (슈퍼 관리자 전용)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/admins")
    public ResponseEntity<CommonResponseDTO<List<GetOneAdminResponse>>> getAdminList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) AdminStatus status,
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "role",
                    direction = Sort.Direction.DESC
            ) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Page<GetOneAdminResponse> response = adminService.getAdminList(userPrincipal, keyword, role, status, pageable);
        return CommonResponseHandler.success(SuccessCode.GET_SUCCESSFUL, response.getContent());
    }

    // 관리자 1명의 정보 상세조회
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OP_ADMIN', 'CS_ADMIN')")
    @GetMapping("/{adminId}")
    public ResponseEntity<CommonResponseDTO<GetOneAdminResponse>> getOne(
            @PathVariable long adminId, @AuthenticationPrincipal UserPrincipal userPrincipal){
        GetOneAdminResponse response = adminService.getAdminDetail(adminId, userPrincipal);
        return CommonResponseHandler.success(SuccessCode.GET_SUCCESSFUL, response);
    }

    // 내 정보 조회
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OP_ADMIN', 'CS_ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<CommonResponseDTO<GetOneAdminResponse>> getMe(
            @AuthenticationPrincipal UserPrincipal userPrincipal){
        Long myId = userPrincipal.getId();
        GetOneAdminResponse response = adminService.getAdminDetail(myId, userPrincipal);
        return CommonResponseHandler.success(SuccessCode.GET_SUCCESSFUL, response);
    }

    // 내 정보 수정
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OP_ADMIN', 'CS_ADMIN')")
    @PutMapping("/me")
    public ResponseEntity<CommonResponseDTO<UpdateAdminResponse>> updateMe(
            @Valid @RequestBody UpdateAdminRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal){
        Long myId = userPrincipal.getId();
        UpdateAdminResponse response = adminService.updateAdminInfo(myId, request, userPrincipal);
        return CommonResponseHandler.success(SuccessCode.DATA_UPDATED, response);
    }

    // 관리자 가입 승인 (슈퍼 관리자 전용)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/{adminId}/approve")
    public ResponseEntity<CommonResponseDTO<Void>> approveAdmin(
            @PathVariable Long adminId, @AuthenticationPrincipal AdminUserDetails userDetails) {
        adminService.approveAdmin(adminId, userDetails.getAdmin().getId());
        return CommonResponseHandler.success(SuccessCode.STATUS_PATCHED);
    }

    // 관리자 가입 거절 (슈퍼 관리자 전용)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/{adminId}/reject")
    public ResponseEntity<CommonResponseDTO<RejectResponse>> rejectAdmin(
            @PathVariable Long adminId, @Valid @RequestBody RejectRequest request,
            @AuthenticationPrincipal AdminUserDetails userDetails) {
        RejectResponse response = adminService.rejectAdmin(adminId, request, userDetails.getAdmin().getId());
        return CommonResponseHandler.success(SuccessCode.STATUS_PATCHED, response);
    }

    // 관리자 정보 수정 (본인이거나 슈퍼 관리자일 경우)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{adminId}")
    public ResponseEntity<CommonResponseDTO<UpdateAdminResponse>> updateAdminInfo(
            @PathVariable Long adminId,
            @Valid @RequestBody UpdateAdminRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UpdateAdminResponse response = adminService.updateAdminInfo(adminId, request, userPrincipal);
        return CommonResponseHandler.success(SuccessCode.DATA_UPDATED,response);
    }

    // 관리자 상태 수정 (슈퍼 관리자 전용)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping("/{adminId}/status")
    public ResponseEntity<CommonResponseDTO<Void>> updateAdminStatus(
            @PathVariable Long adminId, @Valid @RequestBody UpdateAdminStatusRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        adminService.updateAdminStatus(adminId, request.getStatus(), userPrincipal);
        return CommonResponseHandler.success(SuccessCode.DATA_UPDATED);
    }

    // 관리자 삭제 (슈퍼 관리자 전용)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{adminId}")
    public ResponseEntity<CommonResponseDTO<Void>> deleteAdmin(
            @PathVariable Long adminId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        adminService.deleteAdmin(adminId, userPrincipal);
        return CommonResponseHandler.success(SuccessCode.DATA_UPDATED);
    }

    // 관리자 역할 수정 (슈퍼 관리자 전용)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping("/{adminId}/role")
    public ResponseEntity<CommonResponseDTO<Void>> updateAdminRole(
            @PathVariable Long adminId, @Valid @RequestBody UpdateRoleRequest request, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        adminService.updateAdminRole(adminId, request.getRole(), userPrincipal);
        return CommonResponseHandler.success(SuccessCode.DATA_UPDATED);
    }

    // 토큰 재발급 (리프레시 토큰 사용)
    @PostMapping("/reissue")
    public ResponseEntity<CommonResponseDTO<String>> reissue(
            @RequestHeader("Refresh-Token") String refreshToken
    ) {
        String newAccessToken = adminService.reissueAccessToken(refreshToken);
        return CommonResponseHandler.success(SuccessCode.GET_SUCCESSFUL, newAccessToken);
    }
}