package com.example.commerce.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class UpdateAdminRequest {

    // [수정] @NotBlank 제거 (이름 수정 안 할 수도 있음)
    private String name;

    // [수정] @NotBlank 제거 (이메일 수정 안 할 수도 있음)
    @Email(message = "이메일 형식과 일치해야 합니다.")
    private String email;

    @Pattern(regexp = "^01[0-9]-\\d{3,4}-\\d{4}$", message = "전화번호 형식은 010-XXXX-XXXX 입니다.")
    private String phone;
}