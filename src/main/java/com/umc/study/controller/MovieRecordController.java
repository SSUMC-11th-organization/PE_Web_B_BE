package com.umc.study.controller;

import com.umc.study.apiPayload.ApiResponse;
import com.umc.study.config.SwaggerConfig;
import com.umc.study.dto.CreateMovieRecordRequest;
import com.umc.study.dto.MovieRecordResponse;
import com.umc.study.interceptor.AuthInterceptor;
import com.umc.study.service.MovieRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * [8주차] 평점 + 즐겨찾기를 한 트랜잭션으로 저장하는 학습용 API
 */
@Tag(name = "MovieRecord", description = "영화 기록 API (Authorization: Bearer {token} 필요)")
@RestController
@RequiredArgsConstructor
public class MovieRecordController {

    private final MovieRecordService movieRecordService;

    // POST http://localhost:8080/movie-records
    @Operation(summary = "영화 기록 저장 (평점 + 즐겨찾기)",
            description = "addToFavorites=true면 평점과 즐겨찾기를 하나의 트랜잭션으로 저장, 하나라도 실패하면 모두 rollback<br>"
                    + "201 성공 / 400 입력 검증 실패 / 401 인증 실패 / 409 중복 평점(RATING409_1) / 409 중복 즐겨찾기(FAVORITE409_1)")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @PostMapping("/movie-records")
    public ResponseEntity<ApiResponse<MovieRecordResponse>> create(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @Valid @RequestBody CreateMovieRecordRequest request) {
        MovieRecordResponse response = movieRecordService.create(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("영화 기록을 저장했습니다.", response));
    }
}
