package com.umc.movie.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record MemberUpdateRequest(@Schema(description = "변경할 닉네임 (2~12자)", example = "구주차수정") @NotBlank @Size(min = 2, max = 12) String nickname) {}
