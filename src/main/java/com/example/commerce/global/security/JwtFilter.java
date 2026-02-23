package com.example.commerce.global.security;

import com.example.commerce.admin.entity.Admin;
import com.example.commerce.admin.repository.AdminRepository;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. Header에서 토큰 추출
        String bearerToken = request.getHeader("Authorization");
        String token = null;
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            token = bearerToken.substring(7); // "Bearer " 이후의 순수 토큰만 추출
        }

        // 2. 토큰 유효성 검증 및 SecurityContext 에 인증 정보 저장
        if (token != null && jwtUtil.validateToken(token)) {
            Claims claims = jwtUtil.getUserInfoFromToken(token);
            String email = claims.getSubject();

            // DB에서 유저 조회 (유효한 유저인지 최종 확인)
            Admin admin = adminRepository.findByEmail(email).orElse(null);

            // DB에 존재하면서 동시에 상태가 '로그인 가능(ACTIVE)'일 때만 통과(로그인 가능한 시간 도중 정지되어도, 유효한 토큰을 가진 경우 활동가능한 것을 예외처리함)
            if (admin != null && admin.getStatus().isLoginable()) {
                AdminUserDetails userDetails = new AdminUserDetails(admin);

                // 스프링 시큐리티에게 "이 사용자는 인증되었고, 이런 권한을 가졌다"고 알려주는 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // SecurityContext에 등록 (@PreAuthorize가 정상 작동함)
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 다음 필터로 요청을 넘김
        filterChain.doFilter(request, response);
    }
}