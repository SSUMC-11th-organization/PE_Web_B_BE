package com.umc.movie.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record UpdateRatingRequest(
        @NotNull(message = "별점을 입력해주세요.")
        @Min(value = 1, message = "별점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "별점은 5점 이하여야 합니다.")
        @Schema(example = "4") Integer score,
        @Size(max = 500, message = "후기는 500자 이하로 입력해주세요.")
        @Schema(example = "다시 보니 새로운 장면이 보입니다.") String comment
) {}
