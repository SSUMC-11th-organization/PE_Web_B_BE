package com.umc.study.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "umc@example.com")
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        // 로그인 단계에서는 비밀번호 규칙까지 검사하지 않습니다.
        // 규칙이 바뀌기 전에 가입한 회원이 로그인조차 못 하게 되기 때문입니다.
        @Schema(example = "Umc!1234")
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {}
