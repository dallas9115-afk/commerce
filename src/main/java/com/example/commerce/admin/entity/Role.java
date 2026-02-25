package com.example.commerce.admin.entity;

import com.example.commerce.global.exception.ErrorCode;
import com.example.commerce.global.exception.ServiceException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    SUPER_ADMIN("A01", "총관리자",3),
    OP_ADMIN("A02","운영 관리자",2),
    CS_ADMIN("A03","고객 지원 관리자",1);
    // Spring Security 사용을 위해 접두사(ROLE) 추가

    private final String id;
    private final String roleName;
    private final int level;

    // 역할 String 을 Enum 타입으로 변환
    public static Role from(String role) {
        // 입력받은 직책이 공백일 경우
        if (role == null || role.isBlank()) {
            throw new ServiceException(ErrorCode.INVALID_ROLE);
        }

        try {
            // [수정] valueOf는 매칭되는 값이 없으면 즉시 IllegalArgumentException을 던지므로 여기서 찾습니다.
            return Role.valueOf(role);
        } catch (IllegalArgumentException e) {
            // [수정] 500 에러 대신 우리가 만든 커스텀 예외(400)를 던집니다.
            throw new ServiceException(ErrorCode.INVALID_ROLE);
        }
    }
}