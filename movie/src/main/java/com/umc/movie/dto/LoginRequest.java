package com.umc.movie.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@Schema(description = "가입 이메일", example = "week9@umc.com") @NotBlank @Email String email, @Schema(description = "가입 비밀번호", example = "Umc!1234", accessMode = Schema.AccessMode.WRITE_ONLY) @NotBlank String password) {}
