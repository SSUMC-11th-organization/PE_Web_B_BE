package com.umc.study.controller;

import com.umc.study.apiPayload.ApiResponse;
import com.umc.study.config.SwaggerConfig;
import com.umc.study.dto.CreateFavoriteRequest;
import com.umc.study.dto.FavoriteResponse;
import com.umc.study.dto.FavoriteStatusResponse;
import com.umc.study.interceptor.AuthInterceptor;
import com.umc.study.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * [8주차] 즐겨찾기 API. 전부 토큰 필요 (WebConfig 기본값 "모든 경로 보호"로 자동 보호됨)
 */
@Tag(name = "Favorite", description = "즐겨찾기 API (Authorization: Bearer {token} 필요)")
@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    // POST http://localhost:8080/favorites
    @Operation(summary = "즐겨찾기 등록",
            description = "201 성공 / 400 입력 검증 실패 / 401 인증 실패(AUTH401_2~4) / 409 이미 즐겨찾기함(FAVORITE409_1)")
    @PostMapping
    public ResponseEntity<ApiResponse<FavoriteResponse>> create(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @Valid @RequestBody CreateFavoriteRequest request) {
        FavoriteResponse response = favoriteService.create(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("즐겨찾기를 등록했습니다.", response));
    }

    // GET http://localhost:8080/favorites
    @Operation(summary = "내 즐겨찾기 조회",
            description = "200 성공(최신 등록순, 없으면 빈 배열) / 401 인증 실패")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getMyFavorites(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId) {
        List<FavoriteResponse> response = favoriteService.getMyFavorites(memberId);
        return ResponseEntity.ok(ApiResponse.ok("내 즐겨찾기 목록을 조회했습니다.", response));
    }

    // GET http://localhost:8080/favorites/{movieId}
    @Operation(summary = "특정 영화 즐겨찾기 여부 조회",
            description = "200 성공(isFavorite: true/false) / 400 경로 값 형식 오류 / 401 인증 실패")
    @GetMapping("/{movieId}")
    public ResponseEntity<ApiResponse<FavoriteStatusResponse>> getStatus(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @Parameter(description = "TMDB 영화 ID", example = "550") @PathVariable Long movieId) {
        FavoriteStatusResponse response = favoriteService.getStatus(memberId, movieId);
        return ResponseEntity.ok(ApiResponse.ok("즐겨찾기 여부를 조회했습니다.", response));
    }

    // DELETE http://localhost:8080/favorites/{movieId}
    // 공통 응답 형식을 지키기 위해 204 대신 200 + result: null
    @Operation(summary = "즐겨찾기 해제",
            description = "200 성공 / 401 인증 실패 / 404 즐겨찾기하지 않은 영화(FAVORITE404_1)")
    @DeleteMapping("/{movieId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @Parameter(description = "TMDB 영화 ID", example = "550") @PathVariable Long movieId) {
        favoriteService.delete(memberId, movieId);
        return ResponseEntity.ok(ApiResponse.ok("즐겨찾기를 해제했습니다.", null));
    }
}
