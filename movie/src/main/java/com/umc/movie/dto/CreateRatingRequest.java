package com.umc.movie.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record CreateRatingRequest(
        @NotNull(message = "영화 ID를 입력해주세요.")
        @Positive(message = "영화 ID는 양수여야 합니다.")
        @Schema(example = "550") Long movieId,
        @NotNull(message = "별점을 입력해주세요.")
        @Min(value = 1, message = "별점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "별점은 5점 이하여야 합니다.")
        @Schema(example = "5") Integer score,
        @Size(max = 500, message = "후기는 500자 이하로 입력해주세요.")
        @Schema(example = "다시 보고 싶은 영화입니다.") String comment
) {}
