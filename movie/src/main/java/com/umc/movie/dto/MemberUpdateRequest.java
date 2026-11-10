package com.umc.movie.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record MemberUpdateRequest(@NotBlank @Size(min = 2, max = 12) String nickname) {}
