package com.example.commerce.admin.dto;

import com.example.commerce.admin.entity.AdminStatus;
import com.example.commerce.admin.entity.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

public record LoginResponse(
        String token, // 프론트엔드에 전달하는 JWT 토큰
        Long adminId,
        String adminName,
        String adminEmail,
        String adminPhone,
        String adminRole,
        String adminStatus,
        LocalDateTime adminCreatedAt
){}
