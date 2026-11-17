package com.umc.study.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// [8주차] 평점 + 즐겨찾기를 한 번에 저장하는 "영화 기록" 요청
public record CreateMovieRecordRequest(
        @Schema(description = "TMDB 영화 ID", example = "238")
        @NotNull(message = "영화 ID를 입력해주세요.")
        @Positive(message = "영화 ID는 양수여야 합니다.")
        Long movieId,

        @Schema(example = "5")
        @NotNull(message = "별점을 입력해주세요.")
        @Min(value = 1, message = "별점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "별점은 5점 이하여야 합니다.")
        Integer score,

        @Schema(example = "다시 보고 싶은 영화입니다.")
        @Size(max = 500, message = "후기는 500자 이하로 입력해주세요.")
        String comment,

        @Schema(description = "true면 즐겨찾기도 함께 저장", example = "true")
        @NotNull(message = "즐겨찾기 추가 여부를 입력해주세요.")
        Boolean addToFavorites
) {}
