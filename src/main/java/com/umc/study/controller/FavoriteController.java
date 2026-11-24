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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
            description = "🔒 토큰 필요. 토큰의 주인인 회원이 TMDB 영화 ID 하나를 즐겨찾기에 추가합니다.")
    // 우리 공통 응답 DTO 이름도 ApiResponse라서, Swagger 쪽 애노테이션은 풀네임으로 적습니다.
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "즐겨찾기 등록 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "isSuccess": true,
                              "code": "COMMON201",
                              "message": "즐겨찾기를 등록했습니다.",
                              "result": { "favoriteId": 1, "movieId": 550, "createdAt": "2026-11-24T21:00:00" }
                            }
                            """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "입력 검증 실패 (COMMON4001)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "isSuccess": false,
                              "code": "COMMON4001",
                              "message": "영화 ID는 양수여야 합니다.",
                              "result": null
                            }
                            """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "토큰이 없거나(AUTH401_2) 유효하지 않음(AUTH401_3)·만료(AUTH401_4)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "isSuccess": false,
                              "code": "AUTH401_2",
                              "message": "로그인이 필요한 요청입니다.",
                              "result": null
                            }
                            """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "이미 즐겨찾기한 영화 (FAVORITE409_1)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "isSuccess": false,
                              "code": "FAVORITE409_1",
                              "message": "이미 이 영화를 즐겨찾기하셨습니다.",
                              "result": null
                            }
                            """)))
    })
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
            description = "🔒 토큰 필요. 내가 즐겨찾기한 영화 목록을 최신 등록순으로 돌려줍니다.<br>"
                    + "200 성공(없으면 빈 배열) / 401 인증 실패(AUTH401_2·AUTH401_3·AUTH401_4)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getMyFavorites(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId) {
        List<FavoriteResponse> response = favoriteService.getMyFavorites(memberId);
        return ResponseEntity.ok(ApiResponse.ok("내 즐겨찾기 목록을 조회했습니다.", response));
    }

    // GET http://localhost:8080/favorites/{movieId}
    @Operation(summary = "특정 영화 즐겨찾기 여부 조회",
            description = "🔒 토큰 필요. 특정 영화를 내가 즐겨찾기했는지 확인합니다. 안 했어도 오류가 아니라 isFavorite: false입니다.<br>"
                    + "200 성공 / 400 경로 값 형식 오류(COMMON4003) / 401 인증 실패(AUTH401_2·AUTH401_3·AUTH401_4)")
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
            description = "🔒 토큰 필요. 내 즐겨찾기에서 영화 하나를 해제합니다.<br>"
                    + "200 성공 / 401 인증 실패(AUTH401_2·AUTH401_3·AUTH401_4) / 404 즐겨찾기하지 않은 영화(FAVORITE404_1)")
    @DeleteMapping("/{movieId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @Parameter(description = "TMDB 영화 ID", example = "550") @PathVariable Long movieId) {
        favoriteService.delete(memberId, movieId);
        return ResponseEntity.ok(ApiResponse.ok("즐겨찾기를 해제했습니다.", null));
    }
}
