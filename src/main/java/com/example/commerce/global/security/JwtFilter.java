package com.example.commerce.global.security;

import com.example.commerce.admin.entity.Admin;
import com.example.commerce.admin.repository.AdminRepository;
import com.example.commerce.customer.entity.Customer;
import com.example.commerce.customer.entity.CustomerStatus;
import com.example.commerce.customer.repository.CustomerRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AdminRepository adminRepository;
    private final CustomerRepository customerRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String bearerToken = request.getHeader("Authorization");
        String token = null;
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            token = bearerToken.substring(7);
        }

        if (token != null && jwtUtil.validateToken(token)) {
            Claims claims = jwtUtil.getUserInfoFromToken(token);
            String email = claims.getSubject();
            String role = claims.get("role", String.class);

            if ("CUSTOMER".equals(role)) {
                Customer customer = customerRepository.findByEmail(email).orElse(null);

                if (customer != null) {
                    // [수정] 고객 상태가 ACTIVE가 아니면 즉시 커스텀 에러 반환 및 요청 중단
                    if (customer.getStatus() != CustomerStatus.ACTIVE) {
                        sendErrorResponse(response, "정지되거나 비활성화된 고객 계정입니다.");
                        return; // 필터 체인 중단
                    }

                    CustomerUserDetails userDetails = new CustomerUserDetails(customer);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            else {
                Admin admin = adminRepository.findByEmail(email).orElse(null);

                if (admin != null) {
                    // [수정] 관리자 계정이 로그인 불가능 상태면 즉시 커스텀 에러 반환
                    if (!admin.getStatus().isLoginable()) {
                        sendErrorResponse(response, "승인되지 않거나 정지된 관리자 계정입니다.");
                        return; // 필터 체인 중단
                    }

                    AdminUserDetails userDetails = new AdminUserDetails(admin);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    // [추가] 필터단에서 발생하는 비즈니스 예외를 JSON으로 포장해주는 메서드
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
        response.getWriter().write("{\"status\": 403, \"code\": \"INVALID_ACCOUNT_STATUS\", \"message\": \"" + message + "\"}");
    }
}