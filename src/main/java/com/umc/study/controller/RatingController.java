package com.umc.study.controller;

import com.umc.study.apiPayload.ApiResponse;
import com.umc.study.config.SwaggerConfig;
import com.umc.study.dto.CreateRatingRequest;
import com.umc.study.dto.RatingResponse;
import com.umc.study.dto.UpdateRatingRequest;
import com.umc.study.interceptor.AuthInterceptor;
import com.umc.study.service.RatingService;
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
 * [7주차] 클래스 레벨 @RequestMapping("/members")를 제거했습니다.
 * 남아 있으면 /members/ratings가 되어 404가 납니다.
 */
@Tag(name = "Rating", description = "평점 API")
@RestController
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    // GET http://localhost:8080/members/{memberId}/ratings  (공개 API, 토큰 불필요)
    @Operation(summary = "유저별 평점 조회", description = "최신순으로 반환 / 404 존재하지 않는 유저")
    @GetMapping("/members/{memberId}/ratings")
    public List<RatingResponse> getMemberRatings(
            @Parameter(description = "회원 ID", example = "1") @PathVariable Long memberId) {
        return ratingService.getRatingsByMember(memberId);
    }

    // POST http://localhost:8080/ratings  (Authorization: Bearer {token})
    // URL에서 /members/{memberId}가 사라졌습니다. 받지 않는 값은 조작당할 수도 없습니다.
    @Operation(summary = "평점 등록",
            description = "201 성공 / 400 입력 검증 실패 / 401 인증 실패 / 409 중복 평점(RATING409_1)")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @PostMapping("/ratings")
    public ResponseEntity<ApiResponse<RatingResponse>> create(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @Valid @RequestBody CreateRatingRequest request) {
        RatingResponse response = ratingService.create(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("평점을 등록했습니다.", response));
    }

    // PATCH http://localhost:8080/ratings/{ratingId}  (Authorization: Bearer {token})
    @Operation(summary = "평점 수정",
            description = "200 성공 / 400 입력 검증 실패 / 401 인증 실패 / 403 본인 평점 아님(RATING403_1) / 404 없는 평점")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @PatchMapping("/ratings/{ratingId}")
    public ResponseEntity<ApiResponse<RatingResponse>> update(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @Parameter(description = "평점 ID", example = "1") @PathVariable Long ratingId,
            @Valid @RequestBody UpdateRatingRequest request) {
        RatingResponse response = ratingService.update(memberId, ratingId, request);
        return ResponseEntity.ok(ApiResponse.ok("평점을 수정했습니다.", response));
    }

    // DELETE http://localhost:8080/ratings/{ratingId}  (Authorization: Bearer {token})
    // 204는 본문을 보낼 수 없어 공통 응답 형식을 지킬 수 없으므로 200 + result: null
    @Operation(summary = "평점 삭제",
            description = "200 성공 / 401 인증 실패 / 403 본인 평점 아님(RATING403_1) / 404 없는 평점(RATING404_1)")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    @DeleteMapping("/ratings/{ratingId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(hidden = true) @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @Parameter(description = "평점 ID", example = "1") @PathVariable Long ratingId) {
        ratingService.delete(memberId, ratingId);
        return ResponseEntity.ok(ApiResponse.ok("평점을 삭제했습니다.", null));
    }
}
