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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
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

    @PostMapping("/logIn")
    public ResponseEntity<CommonResponseDTO<LoginAdminResponse>> login(
            @Valid @RequestBody LoginAdminRequest request
    ){
        // 1. HttpServletRequest 파라미터 삭제
        // 2. 서비스 호출 시에도 request(DTO)만 넘김
        LoginAdminResponse response = adminService.login(request);

        // JWT 토큰이 포함된 response 를 Data 로 넣어서 반환
        return CommonResponseHandler.success(SuccessCode.LOGIN_SUCCESSFUL, response);
    }

    // 로그아웃 API 신규 구현
    @PostMapping("/logOut")
    public ResponseEntity<CommonResponseDTO<Void>> logout(
            @AuthenticationPrincipal AdminUserDetails userDetails
    ) {
        adminService.logout(userDetails.getAdmin().getId());
        return CommonResponseHandler.success(SuccessCode.DATA_UPDATED); // 또는 LOGOUT_SUCCESSFUL 등 상황에 맞는 코드
    }

    // 관리자 리스트 조회 (슈퍼 관리자 전용)
    // Spring Security가 세션/토큰을 확인하여 ROLE_SUPER_ADMIN이 아니면 403을 반환.
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/admins") //endpoint 수정 (by 권지원, at 02/21 12:35)
    public ResponseEntity<CommonResponseDTO<List<GetOneAdminResponse>>> getAdminList(
            /*
            * RequestParam : URL 주소 뒤에 ? 를 붙이고, key=value 형태로 데이터 보내는 쿼리 스트림을
              Java의 변수로 자동 적용해주는 어노테이션
           * required = false -> 검색조건에서 있어도, 없어도 상관없다면 false 로 되어있어야 에러 방지됨
            (기본적으로 RequestParam 값은 클라이언트가 무조건 보내야 하기 때문)
             */
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

    //수정 필요
    //관리자 1명의 정보 상세조회
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OP_ADMIN', 'CS_ADMIN')")
    @GetMapping("/{adminId}")
    public ResponseEntity<CommonResponseDTO<GetOneAdminResponse>> getOne(
            @PathVariable long adminId, @AuthenticationPrincipal UserPrincipal userPrincipal){

        GetOneAdminResponse response = adminService.getAdminDetail(adminId, userPrincipal);
        return CommonResponseHandler.success(SuccessCode.GET_SUCCESSFUL, response);
    }

    // 그냥 내 정보 조회 (중복 제거 및 AdminDetailResponse 로 통합) -> getMyInfo 삭제
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OP_ADMIN', 'CS_ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<CommonResponseDTO<GetOneAdminResponse>> getMe(
            @AuthenticationPrincipal UserPrincipal userPrincipal){
        Long myId = userPrincipal.getId();
        // 타인 조회 로직에 내 ID를 넣어서 리팩터링
        GetOneAdminResponse response = adminService.getAdminDetail(myId, userPrincipal);
        return CommonResponseHandler.success(SuccessCode.GET_SUCCESSFUL, response);
    }

    // 그냥 내 정보 수정 (중복 제거 및 UpdateAdminRequest 로 통합) -> updateMyInfo 삭제
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OP_ADMIN', 'CS_ADMIN')")
    @PutMapping("/me")
    public ResponseEntity<CommonResponseDTO<UpdateAdminResponse>> updateMe(
            @Valid @RequestBody UpdateAdminRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal){
        Long myId = userPrincipal.getId();
        // 타인 수정 로직에 내 ID를 넣어서 리팩터링
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

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/{adminId}/reject")
    public ResponseEntity<CommonResponseDTO<RejectResponse>> rejectAdmin(
            @PathVariable Long adminId, @Valid @RequestBody RejectRequest request,
            @AuthenticationPrincipal AdminUserDetails userDetails) {


        RejectResponse response = adminService.rejectAdmin(adminId, request, userDetails.getAdmin().getId());

        return CommonResponseHandler.success(SuccessCode.STATUS_PATCHED, response);
    }

    // 관리자 정보 수정 (본인이거나 슈퍼 관리자일 경우)
    // #id는 URL의 {id}를 의미하며, principal.id는 로그인한 사용자의 ID를 의미합니다.
    @PreAuthorize("#adminId == principal.id or hasRole('SUPER_ADMIN')")
    @PutMapping("/{adminId}")
    public ResponseEntity<CommonResponseDTO<UpdateAdminResponse>> updateAdminInfo(
            @PathVariable Long adminId, @RequestBody UpdateAdminRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        UpdateAdminResponse response = adminService.updateAdminInfo(adminId, request, userPrincipal);

        return CommonResponseHandler.success(SuccessCode.DATA_UPDATED,response);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping("/{adminId}/status")
    public ResponseEntity<CommonResponseDTO<Void>> updateAdminStatus(
            @PathVariable Long adminId, @Valid @RequestBody UpdateAdminStatusRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        adminService.updateAdminStatus(adminId, request.getStatus(), userPrincipal);
        return CommonResponseHandler.success(SuccessCode.DATA_UPDATED);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{adminId}")
    public ResponseEntity<CommonResponseDTO<Void>> deleteAdmin(
            @PathVariable Long adminId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        adminService.deleteAdmin(adminId, userPrincipal);
        return CommonResponseHandler.success(SuccessCode.DATA_UPDATED);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping("/{adminId}/role")
    public ResponseEntity<CommonResponseDTO<Void>> updateAdminRole(
            @PathVariable Long adminId, @Valid @RequestBody UpdateRoleRequest request, @AuthenticationPrincipal UserPrincipal userPrincipal) {


        adminService.updateAdminRole(adminId, request.getRole(), userPrincipal);
        return CommonResponseHandler.success(SuccessCode.DATA_UPDATED);
    }

    @PostMapping("/reissue")
    public ResponseEntity<CommonResponseDTO<String>> reissue(
            // 헤더의 "Refresh-Token" 키값으로 받는다고 가정
            //(같이 공부할 포인트) 실무 서비스에서는 프론트엔드가 Header 나 Body에 리프레시 토큰을 담아 보낸다 함.
            @RequestHeader("Refresh-Token") String refreshToken
    ) {
        // 서비스 호출하여 새로운 Access Token 발급
        String newAccessToken = adminService.reissueAccessToken(refreshToken);

        // 새 토큰을 응답으로 반환
        return CommonResponseHandler.success(SuccessCode.GET_SUCCESSFUL, newAccessToken);
    }
}